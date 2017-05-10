package com.webpieces.http2engine.api.client;

import com.webpieces.http2engine.impl.client.Level1ClientEngine;

public class Http2ClientEngineFactory {

	private InjectionConfig injectionConfig;
	
	public Http2ClientEngineFactory(InjectionConfig injectionConfig) {
		this.injectionConfig = injectionConfig;
	}
	/*
	 * TODO: move this to client...
	 * 		HeaderSettings localSettings = new HeaderSettings();
		localSettings.setId(id);
		
		//allow 100 frames of room
		long initialWindowSize = 100 * localSettings.getMaxFrameSize();
		//localSettings.setInitialWindowSize(1000);
		localSettings.setInitialWindowSize(initialWindowSize);
	 */
	public Http2ClientEngine createClientParser(ClientEngineListener resultListener) {
		return new Level1ClientEngine(resultListener, injectionConfig);
	}
	
}
