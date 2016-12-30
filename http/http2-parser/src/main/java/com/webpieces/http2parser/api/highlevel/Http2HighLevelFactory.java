package com.webpieces.http2parser.api.highlevel;

import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser2.impl.stateful.Level0ConnectionParser;

public class Http2HighLevelFactory {

	public Http2StatefulParser createClientParser(String id, Http2Parser2 lowLevelParser, ResultListener resultListener) {
		return new Level0ConnectionParser(id, lowLevelParser, resultListener);
	}
	
}
