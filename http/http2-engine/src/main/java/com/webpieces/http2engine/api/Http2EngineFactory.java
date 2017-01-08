package com.webpieces.http2engine.api;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.impl.HeaderSettings;
import com.webpieces.http2engine.impl.Level1ClientEngine;

public class Http2EngineFactory {

	public Http2ClientEngine createClientParser(HpackParser lowLevelParser, EngineListener resultListener, HeaderSettings localSettings) {
		return new Level1ClientEngine(lowLevelParser, resultListener, localSettings);
	}
	
	public Http2ClientEngine createClientParser(String id, HpackParser lowLevelParser, EngineListener resultListener) {
		HeaderSettings localSettings = new HeaderSettings();
		localSettings.setId(id);
		
		//allow 100 frames of room
		long initialWindowSize = 100 * localSettings.getMaxFrameSize();
		//localSettings.setInitialWindowSize(1000);
		localSettings.setInitialWindowSize(initialWindowSize);
		return new Level1ClientEngine(lowLevelParser, resultListener, localSettings);
	}
}
