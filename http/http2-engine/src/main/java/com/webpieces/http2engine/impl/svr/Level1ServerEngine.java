package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.api.error.FarEndClosedConnection;
import com.webpieces.http2engine.api.error.UserInitiatedConnectionClose;
import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.RemoteSettingsManagement;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;
import com.webpieces.util.locking.PermitQueue;

public class Level1ServerEngine implements Http2ServerEngine {

	private static final Logger log = LoggerFactory.getLogger(Level1ServerEngine.class);
	
	private Level7MarshalAndPing marshalLayer;
	private Level2ParsingAndRemoteSettings parsing;
	private Level3SvrIncomingSynchro incomingSync;
	private Level3SvrOutgoingSynchro outgoingSync;
	private String logId;

	public Level1ServerEngine(String logId, ServerEngineListener listener, InjectionConfig injectionConfig) {
		this.logId = logId;
		Http2Config config = injectionConfig.getConfig();
		HpackParser parser = injectionConfig.getLowLevelParser();
		HeaderSettings remoteSettings = new HeaderSettings();
		HeaderSettings localSettings = config.getLocalSettings();

		PermitQueue maxConcurrent = new PermitQueue(100);

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState(injectionConfig.getTime(), logId);


		Level8NotifySvrListeners finalLayer = new Level8NotifySvrListeners(listener, this);
		marshalLayer = new Level7MarshalAndPing(logId, parser, remoteSettings, finalLayer);
		Level6RemoteFlowControl remoteFlowCtrl = new Level6RemoteFlowControl(logId, streamState, marshalLayer, remoteSettings);
		Level6SvrLocalFlowControl localFlowCtrl = new Level6SvrLocalFlowControl(logId, marshalLayer, finalLayer, localSettings);
		Level5ServerStateMachine clientSm = new Level5ServerStateMachine(logId, streamState, remoteFlowCtrl, localFlowCtrl, localSettings, remoteSettings, maxConcurrent);
		Level4ServerPreconditions streamInit = new Level4ServerPreconditions(logId, clientSm);

		outgoingSync = new Level3SvrOutgoingSynchro(maxConcurrent, streamInit, marshalLayer, localSettings);
		RemoteSettingsManagement mgmt = new RemoteSettingsManagement(outgoingSync, remoteFlowCtrl, marshalLayer, remoteSettings);
		incomingSync = new Level3SvrIncomingSynchro(streamInit, marshalLayer, mgmt);
		
		parsing = new Level2ServerParsing(logId, incomingSync, outgoingSync, marshalLayer, parser, config);
	}

	@Override
	public CompletableFuture<Void> intialize() {
		return outgoingSync.sendSettingsToSocket();
	}
	
	@Override
	public CompletableFuture<Void> sendPing() {
		return marshalLayer.sendPing();
	}

	@Override
	public void parse(DataWrapper newData) {
		parsing.parse(newData);
	}

	@Override
	public void farEndClosed() {
		log.error(logId+"Far end closing");
		FarEndClosedConnection reset = new FarEndClosedConnection(logId+"Far end sent goaway to us");
		incomingSync.sendGoAwayToApp(reset).exceptionally( t -> {
			log.error(logId+"Exception after remote socket closed resetting streams.", t);
			return null;
		});
	}

	@Override
	public void initiateClose(String reason) {
		UserInitiatedConnectionClose close = new UserInitiatedConnectionClose(reason);
		incomingSync.sendGoAwayToSvrAndResetAllToApp(close);
	}

	public CompletableFuture<StreamWriter> sendResponseHeaders(Stream stream, Http2Response data) {
		int streamId = data.getStreamId();
		if(streamId <= 0)
			throw new IllegalArgumentException(logId+"frames for requests must have a streamId > 0");
		else if(streamId % 2 == 0)
			throw new IllegalArgumentException(logId+"Server cannot send response frames with even stream ids to client per http/2 spec");
		
		return outgoingSync.sendResponseToSocket(stream, data);
	}

	public CompletableFuture<PushPromiseListener> sendPush(PushStreamHandleImpl handle, Http2Push push) {
		int streamId = push.getStreamId();
		int promisedId = push.getPromisedStreamId();
		if(streamId <= 0 || promisedId <= 0)
			throw new IllegalArgumentException("push frames for requests must have a streamId and promisedStreamId > 0");
		else if(streamId % 2 == 0)
			throw new IllegalArgumentException("Server cannot send push frames with even stream ids to client per http/2 spec");
		else if(promisedId % 2 == 1)
			throw new IllegalArgumentException("Server cannot send push frames with odd promisedStreamId to client per http/2 spec");				

		return outgoingSync.sendPushToSocket(handle, push).thenApply(s -> new PushPromiseEngineListener(s) );
	}

	private class PushPromiseEngineListener implements PushPromiseListener {
		private ServerPushStream stream;
		public PushPromiseEngineListener(ServerPushStream stream) {
			this.stream = stream;
		}

		@Override
		public CompletableFuture<StreamWriter> incomingPushResponse(Http2Response response) {
			int streamId = response.getStreamId();
			if(streamId != stream.getStreamId())
				throw new IllegalArgumentException("response frame must have the same stream id as the push msg and did not.  pushStreamId="+stream.getStreamId()+" frame="+response);

			return outgoingSync.sendPushResponseToSocket(stream, response).thenApply(v -> {
				return new PushEngineWriter(stream); 
			});
		}
	}
	
	private class PushEngineWriter implements StreamWriter {
		private ServerPushStream stream;
		public PushEngineWriter(ServerPushStream stream) {
			this.stream = stream;
		}

		@Override
		public CompletableFuture<StreamWriter> processPiece(StreamMsg data) {
			int streamId = data.getStreamId();
			if(streamId != stream.getStreamId())
				throw new IllegalArgumentException("response frame must have the same stream id as the push msg and did not.  pushStreamId="+stream.getStreamId()+" frame="+data);

			return outgoingSync.sendDataToSocket(stream, data).thenApply(v -> this);
		}
	}
	
	public CompletableFuture<Void> sendCancel(Stream stream, RstStreamFrame frame) {
		int streamId = frame.getStreamId();
		if(streamId <= 0)
			throw new IllegalArgumentException("frames for requests must have a streamId > 0");
		else if(streamId % 2 == 0)
			throw new IllegalArgumentException("Server cannot send response frames with even stream ids to client per http/2 spec");
		
		return outgoingSync.sendRstToSocket(stream, frame);
	}
	
	public CompletableFuture<Void> cancelPush(CancelReason reset) {
		int streamId = reset.getStreamId();
		if(streamId <= 0)
			throw new IllegalArgumentException("frames for requests must have a streamId > 0");
		else if(streamId % 2 == 1)
			throw new IllegalArgumentException("Server cannot send reset frame with odd stream ids to client per http/2 spec");
		
		return outgoingSync.sendPushRstToSocket(reset);
	}
}
