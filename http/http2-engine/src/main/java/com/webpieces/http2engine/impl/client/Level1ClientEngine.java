package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.locking.PermitQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.RequestStreamHandle;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.api.client.Http2ClientEngine;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.api.error.FarEndClosedConnection;
import com.webpieces.http2engine.api.error.UserInitiatedConnectionClose;
import com.webpieces.http2engine.impl.shared.Level2ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.RemoteSettingsManagement;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

public class Level1ClientEngine implements Http2ClientEngine {
	
	private static final Logger log = LoggerFactory.getLogger(Level1ClientEngine.class);

	private Level7MarshalAndPing marshalLayer;
	private Level3ClntIncomingSynchro incomingSyncro;
	private Level3ClntOutgoingSyncro outgoingSyncro;
	private Level2ParsingAndRemoteSettings parsing;
	private AtomicInteger nextAvailableStreamId = new AtomicInteger(1);

	private String key;

	public Level1ClientEngine(String logId, ClientEngineListener clientEngineListener, InjectionConfig injectionConfig) {
		this.key = logId;
		Http2Config config = injectionConfig.getConfig();
		HpackParser parser = injectionConfig.getLowLevelParser();
		HeaderSettings remoteSettings = new HeaderSettings();
		HeaderSettings localSettings = config.getLocalSettings();

		PermitQueue permitQueue = new PermitQueue(config.getInitialRemoteMaxConcurrent());

		//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
		//we have to release items in the map inside this or release the engine
		StreamState streamState = new StreamState(injectionConfig.getTime(), logId);
		
		Level8NotifyClntListeners finalLayer = new Level8NotifyClntListeners(clientEngineListener);
		marshalLayer = new Level7MarshalAndPing(logId, parser, remoteSettings, finalLayer);
		Level6RemoteFlowControl remoteFlowCtrl = new Level6RemoteFlowControl(logId, streamState, marshalLayer, remoteSettings);
		Level6ClntLocalFlowControl localFlowCtrl = new Level6ClntLocalFlowControl(logId, marshalLayer, finalLayer, localSettings);
		Level5ClientStateMachine clientSm = new Level5ClientStateMachine(logId, streamState, remoteFlowCtrl, localFlowCtrl, config, remoteSettings, permitQueue);
		Level4ClientPreconditions streamInit = new Level4ClientPreconditions(clientSm);

		outgoingSyncro = new Level3ClntOutgoingSyncro(permitQueue, streamInit, remoteFlowCtrl, marshalLayer, localSettings, finalLayer);
		RemoteSettingsManagement mgmt = new RemoteSettingsManagement(outgoingSyncro, remoteFlowCtrl, marshalLayer, remoteSettings);
		incomingSyncro = new Level3ClntIncomingSynchro(streamInit, marshalLayer, mgmt, finalLayer);

		parsing = new Level2ClientParsing(logId, incomingSyncro, outgoingSyncro, marshalLayer, parser, config);

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
	public RequestStreamHandle openStream() {
		return new ClientStreamHandle(nextAvailableStreamId, outgoingSyncro);
	}

	@Override
	public CompletableFuture<Void> parse(DataWrapper newData) {
		return parsing.parse(newData);
	}

	@Override
	public void farEndClosed() {
		FarEndClosedConnection reset = new FarEndClosedConnection(key+" The remote end killed the socket(without sending goAway..weird)");
		incomingSyncro.sendGoAwayToApp(reset).exceptionally( t -> {
			log.error(key+"Exception after remote socket closed resetting streams.", t);
			return null;
		});
	}

	@Override
	public void initiateClose(String reason) {
		UserInitiatedConnectionClose close = new UserInitiatedConnectionClose(reason);
		incomingSyncro.sendGoAwayToSvrAndResetAllToApp(close);
	}

}
