package com.webpieces.http2engine.impl.client;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.impl.shared.Level3IncomingSynchro;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.RemoteSettingsManagement;
import com.webpieces.util.locking.FuturePermitQueue;

public class Level3ClntIncomingSynchro extends Level3IncomingSynchro {

	private Level4ClientStreams streams;

	public Level3ClntIncomingSynchro(			
			FuturePermitQueue serializer,
			Level4ClientStreams streamsLayer, 
			Level7MarshalAndPing notifyListener,
			RemoteSettingsManagement remoteSettings,
			Level8NotifyClntListeners finalLayer
	) {
		super(serializer, streamsLayer, notifyListener, remoteSettings);
		streams = streamsLayer;
	}

	public CompletableFuture<Void> processResponse(Http2Response msg) {
		return serializer.runRequest( () -> {
			return streams.sendResponseToApp(msg);
		});
	}

	public CompletableFuture<Void> processPush(Http2Push msg) {
		return serializer.runRequest( () -> {
			return streams.sendPushToApp(msg);
		});
	}






}
