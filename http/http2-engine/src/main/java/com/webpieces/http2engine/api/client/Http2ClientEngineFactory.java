package com.webpieces.http2engine.api.client;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.impl.client.Level1ClientEngine;

public class Http2ClientEngineFactory {

	/*
	 * TODO: move this to client...
	 * 		HeaderSettings localSettings = new HeaderSettings();
		localSettings.setId(id);
		
		//allow 100 frames of room
		long initialWindowSize = 100 * localSettings.getMaxFrameSize();
		//localSettings.setInitialWindowSize(1000);
		localSettings.setInitialWindowSize(initialWindowSize);
	 */
	public Http2ClientEngine createClientParser(Http2Config config, HpackParser lowLevelParser, ClientEngineListener resultListener) {
		return new Level1ClientEngine(config, lowLevelParser, resultListener);
	}
	
}
