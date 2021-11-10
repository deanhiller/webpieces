package com.webpieces.http2engine.impl.svr;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2engine.impl.shared.Level3IncomingSynchro;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.RemoteSettingsManagement;

public class Level3SvrIncomingSynchro extends Level3IncomingSynchro {


	private Level4ServerPreconditions streams;
	
	public Level3SvrIncomingSynchro(			
			Level4ServerPreconditions streamsLayer, 
			Level7MarshalAndPing notifyListener,
			RemoteSettingsManagement remoteSettings
	) {
		super(streamsLayer, notifyListener, remoteSettings);
		streams = streamsLayer;
	}

	public XFuture<Void> processRequest(Http2Request msg) {
		return streams.sendRequestToApp(msg);
	}


}
