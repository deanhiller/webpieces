package com.webpieces.http2parser.api;


import org.webpieces.data.api.BufferPool;

import com.webpieces.http2parser.api.highlevel.Http2StatefulParser;
import com.webpieces.http2parser.api.highlevel.ResultListener;
import com.webpieces.http2parser.impl.Http2ParserImpl;
import com.webpieces.http2parser2.impl.Http2Parser2Impl;
import com.webpieces.http2parser2.impl.stateful.Level0ConnectionParser;

public class Http2ParserFactory {
	
	public static Http2StatefulParser createClientParseForSingleConnection(String id, Http2Parser2 lowLevelParser, ResultListener socketListener) {
		return new Level0ConnectionParser(id, lowLevelParser, socketListener);
	}
	
    public static Http2Parser createParser(BufferPool bufferPool) {
        return new Http2ParserImpl(bufferPool);
    }

	public static Http2Parser2 createParser2(BufferPool bufferCreationPool) {
		return new Http2Parser2Impl(bufferCreationPool);
	}
}
