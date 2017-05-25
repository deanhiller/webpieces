package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.api.client.Http2ClientEngine;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.RemoteSettingsManagement;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.util.locking.FuturePermitQueue;
import com.webpieces.util.locking.PermitQueue;

public class Level1ClientEngine implements Http2ClientEngine {
	
	private static final Logger log = LoggerFactory.getLogger(Level1ClientEngine.class);

	private Level7MarshalAndPing marshalLayer;
	private Level3ClntIncomingSynchro incomingSyncro;
	private Level3ClntOutgoingSyncro outgoingSyncro;
	private Level2ParsingAndRemoteSettings parsing;
	private AtomicInteger nextAvailableStreamId = new AtomicInteger(1);

	public Level1ClientEngine(String key, ClientEngineListener clientEngineListener, InjectionConfig injectionConfig) {
		Http2Config config = injectionConfig.getConfig();
		HpackParser parser = injectionConfig.getLowLevelParser();
		HeaderSettings remoteSettings = new HeaderSettings();
		HeaderSettings localSettings = config.getLocalSettings();

		FuturePermitQueue serializer = new FuturePermitQueue(key, 1);
		PermitQueue permitQueue = new PermitQueue(config.getInitialRemoteMaxConcurrent());

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState(injectionConfig.getTime(), permitQueue);
		
		Level8NotifyClntListeners finalLayer = new Level8NotifyClntListeners(clientEngineListener);
		marshalLayer = new Level7MarshalAndPing(parser, remoteSettings, finalLayer);
		Level6RemoteFlowControl remoteFlowCtrl = new Level6RemoteFlowControl(streamState, marshalLayer, remoteSettings);
		Level6ClntLocalFlowControl localFlowCtrl = new Level6ClntLocalFlowControl(marshalLayer, finalLayer, localSettings);
		Level5ClientStateMachine clientSm = new Level5ClientStateMachine(config.getId(), remoteFlowCtrl, localFlowCtrl);
		Level4ClientStreams streamInit = new Level4ClientStreams(streamState, clientSm, localFlowCtrl, remoteFlowCtrl, config, remoteSettings);

		outgoingSyncro = new Level3ClntOutgoingSyncro(serializer, permitQueue, streamInit, remoteFlowCtrl, marshalLayer, localSettings, finalLayer);
		RemoteSettingsManagement mgmt = new RemoteSettingsManagement(outgoingSyncro, remoteFlowCtrl, marshalLayer, remoteSettings);
		incomingSyncro = new Level3ClntIncomingSynchro(serializer, streamInit, marshalLayer, mgmt, finalLayer);

		parsing = new Level2ClientParsing(incomingSyncro, outgoingSyncro, marshalLayer, parser, config);

	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return marshalLayer.sendPing();
	}
	
	@Override
	public CompletableFuture<Void> sendInitializationToSocket() {
		return outgoingSyncro.sendInitializationToSocket();
	}
	
	@Override
	public StreamHandle openStream(ResponseHandler2 responseListener) {
		return new ClientStreamHandle(nextAvailableStreamId, outgoingSyncro, responseListener);
	}

	@Override
	public CompletableFuture<Void> parse(DataWrapper newData) {
		return parsing.parse(newData);
	}

	@Override
	public void farEndClosed() {
		ConnectionReset reset = new ConnectionReset("Far end sent goaway to us", null, true);
		incomingSyncro.sendGoAwayToApp(reset).exceptionally( t -> {
			log.error("Exception after remote socket closed resetting streams.", t);
			return null;
		});
	}

	@Override
	public void initiateClose(String reason) {
		outgoingSyncro.initiateClose(reason);
	}
}
