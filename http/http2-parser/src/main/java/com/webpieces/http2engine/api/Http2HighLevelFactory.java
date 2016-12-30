package com.webpieces.http2engine.api;

import com.webpieces.http2engine.impl.Level0ConnectionParser;
import com.webpieces.http2parser.api.Http2Parser2;

public class Http2HighLevelFactory {

	public Http2StatefulParser createClientParser(String id, Http2Parser2 lowLevelParser, ResultListener resultListener) {
		return new Level0ConnectionParser(id, lowLevelParser, resultListener);
	}
	
}
