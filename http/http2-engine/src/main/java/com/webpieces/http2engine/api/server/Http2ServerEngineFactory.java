package com.webpieces.http2engine.api.server;


import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.svr.Level1ServerEngine;

public class Http2ServerEngineFactory {

	private InjectionConfig injectionConfig;

	public Http2ServerEngineFactory(InjectionConfig injectionConfig) {
		this.injectionConfig = injectionConfig;
	}
	
	public Http2ServerEngine createEngine(ServerEngineListener listener) {
		return new Level1ServerEngine(listener, injectionConfig);
	}

}
