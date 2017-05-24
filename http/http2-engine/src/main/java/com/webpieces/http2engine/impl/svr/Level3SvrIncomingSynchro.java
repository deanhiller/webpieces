package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.impl.shared.Level3IncomingSynchro;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.RemoteSettingsManagement;
import com.webpieces.util.locking.FuturePermitQueue;

public class Level3SvrIncomingSynchro extends Level3IncomingSynchro {


	private Level4ServerStreams streams;
	
	public Level3SvrIncomingSynchro(			
			FuturePermitQueue serializer,
			Level4ServerStreams streamsLayer, 
			Level7MarshalAndPing notifyListener,
			RemoteSettingsManagement remoteSettings
	) {
		super(serializer, streamsLayer, notifyListener, remoteSettings);
		streams = streamsLayer;
	}

	public CompletableFuture<Void> processRequest(Http2Request msg) {
		return serializer.runRequest( () -> {
			return streams.sendRequestToApp(msg);
		});
	}


}
