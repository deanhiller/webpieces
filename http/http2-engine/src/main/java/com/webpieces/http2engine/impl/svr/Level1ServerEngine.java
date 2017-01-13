package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.ServerEngineListener;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level3StreamInitialization;
import com.webpieces.http2engine.impl.shared.Level4ClientStateMachine;
import com.webpieces.http2engine.impl.shared.Level5LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level5RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level6MarshalAndPing;
import com.webpieces.http2engine.impl.shared.StreamState;

public class Level1ServerEngine implements Http2ServerEngine {

	private Level6MarshalAndPing notifyListener;
	private Level6NotifySvrListeners finalLayer;
	private Level3StreamInitialization streamInit;
	private Level2ParsingAndRemoteSettings parsing;

	public Level1ServerEngine(HpackParser parser, ServerEngineListener listener, HeaderSettings localSettings, Executor backupPool) {
		HeaderSettings remoteSettings = new HeaderSettings();

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState();
		
		finalLayer = new Level6NotifySvrListeners(listener);
		notifyListener = new Level6MarshalAndPing(parser, remoteSettings, finalLayer);
		Level5RemoteFlowControl remoteFlowCtrl = new Level5RemoteFlowControl(streamState, notifyListener, remoteSettings);
		Level5LocalFlowControl localFlowCtrl = new Level5LocalFlowControl(notifyListener, localSettings);
		Level4ClientStateMachine clientSm = new Level4ClientStateMachine(localSettings.getId(), backupPool, remoteFlowCtrl, localFlowCtrl);
		streamInit = new Level3StreamInitialization(streamState, clientSm, remoteFlowCtrl, localSettings, remoteSettings, backupPool);
		parsing = new Level2ParsingAndRemoteSettings(streamInit, remoteFlowCtrl, notifyListener, parser, localSettings, remoteSettings);
		
		//first thing server always has to do is send preface and settings frame
		//I hate calling business logic in a constructor but this does reduce the api's surface area
		//making it so we don't need to throw exceptions if some init method hasn't been called yet.
		parsing.sendSettings();
	}

	@Override
	public CompletableFuture<Void> sendPing() {
		return notifyListener.sendPing();
	}

	@Override
	public void parse(DataWrapper newData) {
		parsing.parse(newData);
	}

	@Override
	public void farEndClosed() {
		notifyListener.farEndClosed();
	}

	@Override
	public void initiateClose(String reason) {
	}
}
