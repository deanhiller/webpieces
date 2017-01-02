package com.webpieces.http2engine.api;

import com.webpieces.http2engine.impl.Level0ClientEngine;
import com.webpieces.http2parser.api.Http2Parser2;

public class Http2EngineFactory {

	public Http2ClientEngine createClientParser(String id, Http2Parser2 lowLevelParser, EngineListener resultListener) {
		return new Level0ClientEngine(id, lowLevelParser, resultListener);
	}
}
