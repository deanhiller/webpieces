package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.api.client.Http2ClientEngine;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level3ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.StreamState;

public class Level1ClientEngine implements Http2ClientEngine {
	
	private Level7MarshalAndPing marshalLayer;
	private Level2ClientSynchro synchronization;

	public Level1ClientEngine(ClientEngineListener clientEngineListener, InjectionConfig injectionConfig) {
		SessionExecutor executor = injectionConfig.getExecutor();
		Http2Config config = injectionConfig.getConfig();
		HpackParser parser = injectionConfig.getLowLevelParser();
		HeaderSettings remoteSettings = new HeaderSettings();
		HeaderSettings localSettings = config.getLocalSettings();

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState(injectionConfig.getTime());
		
		Level8NotifyListeners finalLayer = new Level8NotifyListeners(clientEngineListener);
		marshalLayer = new Level7MarshalAndPing(parser, remoteSettings, finalLayer);
		Level6RemoteFlowControl remoteFlowCtrl = new Level6RemoteFlowControl(streamState, marshalLayer, remoteSettings);
		Level6LocalFlowControl localFlowCtrl = new Level6LocalFlowControl(marshalLayer, finalLayer, localSettings);
		Level5ClientStateMachine clientSm = new Level5ClientStateMachine(config.getId(), remoteFlowCtrl, localFlowCtrl);
		Level4ClientStreams streamInit = new Level4ClientStreams(streamState, clientSm, localFlowCtrl, remoteFlowCtrl, config, remoteSettings);
		Level3ParsingAndRemoteSettings parsing = new Level3ParsingAndRemoteSettings(streamInit, remoteFlowCtrl, marshalLayer, parser, config, remoteSettings);
		synchronization = new Level2ClientSynchro(streamInit, parsing, finalLayer, executor);

	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return marshalLayer.sendPing();
	}
	
	@Override
	public CompletableFuture<Void> sendInitializationToSocket() {
		return synchronization.sendInitializationToSocket();
	}
	
	@Override
	public CompletableFuture<StreamWriter> sendFrameToSocket(Http2Headers headers, Http2ResponseListener responseListener) {
		return synchronization.sendRequestToSocket(headers, responseListener);
	}

	@Override
	public void parse(DataWrapper newData) {
		synchronization.parse(newData);

	}

	@Override
	public void farEndClosed() {
		synchronization.farEndClosed();
	}

	@Override
	public void initiateClose(String reason) {
		synchronization.initiateClose(reason);
	}
}
