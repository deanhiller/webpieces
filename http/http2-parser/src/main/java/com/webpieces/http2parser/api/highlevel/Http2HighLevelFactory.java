package com.webpieces.http2parser.api.highlevel;

import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser2.impl.stateful.Http2ClientParserImpl;

public class Http2HighLevelFactory {

	public Http2StatefulParser createClientParser(String id, Http2Parser2 lowLevelParser, ToClient clientListener, ToSocket socketListener) {
		return new Http2ClientParserImpl(id, lowLevelParser, clientListener, socketListener);
	}
	
}
