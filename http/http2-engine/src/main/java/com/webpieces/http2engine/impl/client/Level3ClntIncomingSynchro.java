package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.impl.shared.Level3IncomingSynchro;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.RemoteSettingsManagement;

public class Level3ClntIncomingSynchro extends Level3IncomingSynchro {

	private Level4ClientPreconditions streams;

	public Level3ClntIncomingSynchro(
			Level4ClientPreconditions streamsLayer, 
			Level7MarshalAndPing notifyListener,
			RemoteSettingsManagement remoteSettings,
			Level8NotifyClntListeners finalLayer
	) {
		super(streamsLayer, notifyListener, remoteSettings);
		streams = streamsLayer;
	}

	public CompletableFuture<Void> sendResponseToApp(Http2Response msg) {
		return streams.sendResponseToApp(msg);
	}

	public CompletableFuture<Void> sendPushToApp(Http2Push msg) {
		return streams.sendPushToApp(msg);
	}

}
