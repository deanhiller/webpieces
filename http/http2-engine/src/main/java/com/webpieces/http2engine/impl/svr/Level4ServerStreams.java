package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.impl.shared.Level4AbstractStreamMgr;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.ParseFailReason;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level4ServerStreams extends Level4AbstractStreamMgr<ServerStream> {

	private final static Logger log = LoggerFactory.getLogger(Level4ServerStreams.class);

	private Level5ServerStateMachine serverSm;
	private HeaderSettings localSettings;
	private volatile int streamsInProcess = 0;
	
	public Level4ServerStreams(StreamState streamState, Level5ServerStateMachine serverSm, Level6LocalFlowControl localFlowControl,
			Level6RemoteFlowControl remoteFlowCtrl, HeaderSettings localSettings, HeaderSettings remoteSettings) {
		super(serverSm, remoteFlowCtrl, localFlowControl, remoteSettings, streamState);
		this.serverSm = serverSm;
		this.localSettings = localSettings;
	}

	public CompletableFuture<Void> sendRequestToApp(Http2Request request) {
		if(request.getStreamId() % 2 == 0)
			throw new ConnectionException(ParseFailReason.BAD_STREAM_ID, request.getStreamId(), "Bad stream id.  Even stream ids not allowed in requests to a server request="+request);
		if(!streamState.isLargeEnough(request))
			throw new StreamException(ParseFailReason.CLOSED_STREAM, request.getStreamId(), "Stream id too low and stream not exist(ie. stream was closed) request="+request);
		
		ServerStream stream = createStream(request.getStreamId());
		return serverSm.fireToClient(stream, request).thenApply(s -> null);
	}
	
	private ServerStream createStream(int streamId) {
		Memento initialState = serverSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ServerStream stream = new ServerStream(streamId, initialState, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}

	private ServerPushStream createPushStream(PushStreamHandleImpl handle, int streamId) {
		Memento initialState = serverSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		ServerPushStream stream = new ServerPushStream(handle, streamId, initialState, localWindowSize, remoteWindowSize);
		streamState.create(stream);
		return stream;
	}
	
	@Override
	public CompletableFuture<Void> sendPayloadToApp(PartialStream frame) {
		Stream stream = streamState.getStream(frame, false);
		
		CompletableFuture<Void> future = serverSm.fireToClient(stream, frame);
		checkForClosedState(stream, frame, false);
		return future;
	}
	
	public CompletableFuture<ServerPushStream> sendPush(PushStreamHandleImpl handle, Http2Push push) {
		int newStreamId = push.getPromisedStreamId();
		ServerPushStream stream = createPushStream(handle, newStreamId);

		return serverSm.fireToSocket(stream, push)
				.thenApply(s -> stream);
	}

	public CompletableFuture<Void> sendResponseHeaders(Stream stream, Http2Response response) {
		if(closedReason != null) {
			return createExcepted(response, "sending response");
		}

		CompletableFuture<Void> future = serverSm.fireToSocket(stream, response);
		checkForClosedState(stream, response, false);
		return future;
	}



}
