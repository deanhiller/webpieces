package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level3ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2engine.impl.shared.StreamState;

public class Level1ServerEngine implements Http2ServerEngine {

	private Level7MarshalAndPing marshalLayer;
	private Level3ParsingAndRemoteSettings parsing;
	private Level2ServerSynchro synchronization;

	public Level1ServerEngine(ServerEngineListener listener, InjectionConfig injectionConfig) {
		SessionExecutor executor = injectionConfig.getExecutor();
		Http2Config config = injectionConfig.getConfig();
		HpackParser parser = injectionConfig.getLowLevelParser();
		HeaderSettings remoteSettings = new HeaderSettings();
		HeaderSettings localSettings = config.getLocalSettings();

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState(injectionConfig.getTime());
		
		Level8NotifySvrListeners finalLayer = new Level8NotifySvrListeners(listener, this);
		marshalLayer = new Level7MarshalAndPing(parser, remoteSettings, finalLayer);
		Level6RemoteFlowControl remoteFlowCtrl = new Level6RemoteFlowControl(streamState, marshalLayer, remoteSettings);
		Level6LocalFlowControl localFlowCtrl = new Level6LocalFlowControl(marshalLayer, finalLayer, localSettings);
		Level5ServerStateMachine clientSm = new Level5ServerStateMachine(config.getId(), remoteFlowCtrl, localFlowCtrl);
		Level4ServerStreams streamInit = new Level4ServerStreams(streamState, clientSm, localFlowCtrl, remoteFlowCtrl, localSettings, remoteSettings);
		parsing = new Level3ParsingAndRemoteSettings(streamInit, remoteFlowCtrl, marshalLayer, parser, config, remoteSettings);
		synchronization = new Level2ServerSynchro(streamInit, parsing, executor);
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

	public CompletableFuture<StreamWriter> sendResponseHeaders(Stream stream, Http2Headers data) {
		return synchronization.sendResponseHeaders(stream, data);
	}

	public CompletableFuture<StreamWriter> sendPush(Http2Push push) {
		return synchronization.sendPush(push);
	}
}
