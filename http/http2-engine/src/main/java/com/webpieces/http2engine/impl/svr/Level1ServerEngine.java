package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level5LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level5RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level6MarshalAndPing;
import com.webpieces.http2engine.impl.shared.StreamState;

public class Level1ServerEngine implements Http2ServerEngine {

	private Level6MarshalAndPing marshalLayer;
	private Level7NotifySvrListeners finalLayer;
	private Level3ServerStreams streamInit;
	private Level2ParsingAndRemoteSettings parsing;

	public Level1ServerEngine(Http2Config config, HpackParser parser, ServerEngineListener listener) {
		HeaderSettings remoteSettings = new HeaderSettings();
		HeaderSettings localSettings = config.getLocalSettings();

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState();
		
		finalLayer = new Level7NotifySvrListeners(listener);
		marshalLayer = new Level6MarshalAndPing(parser, remoteSettings, finalLayer);
		Level5RemoteFlowControl remoteFlowCtrl = new Level5RemoteFlowControl(streamState, marshalLayer, remoteSettings);
		Level5LocalFlowControl localFlowCtrl = new Level5LocalFlowControl(marshalLayer, finalLayer, localSettings);
		Level4ServerStateMachine clientSm = new Level4ServerStateMachine(config.getId(), remoteFlowCtrl, localFlowCtrl);
		streamInit = new Level3ServerStreams(streamState, clientSm, remoteFlowCtrl, localSettings, remoteSettings);
		parsing = new Level2ParsingAndRemoteSettings(streamInit, remoteFlowCtrl, marshalLayer, parser, config, remoteSettings);
	}

	@Override
	public void intialize() {
		parsing.sendSettings();
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
		marshalLayer.farEndClosed();
	}

	@Override
	public void initiateClose(String reason) {
		throw new UnsupportedOperationException("easy to add but didn't add this quite yet");
	}
}
