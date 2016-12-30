package com.webpieces.http2parser.api;


import org.webpieces.data.api.BufferPool;

import com.webpieces.http2engine.api.Http2ClientEngine;
import com.webpieces.http2engine.api.ResultListener;
import com.webpieces.http2engine.impl.Level0ClientEngine;
import com.webpieces.http2parser.impl.Http2ParserImpl;
import com.webpieces.http2parser2.impl.Http2Parser2Impl;

public class Http2ParserFactory {
	
	public static Http2ClientEngine createClientParseForSingleConnection(String id, Http2Parser2 lowLevelParser, ResultListener socketListener) {
		return new Level0ClientEngine(id, lowLevelParser, socketListener);
	}
	
    public static Http2Parser createParser(BufferPool bufferPool) {
        return new Http2ParserImpl(bufferPool);
    }

	public static Http2Parser2 createParser2(BufferPool bufferCreationPool) {
		return new Http2Parser2Impl(bufferCreationPool);
	}
}
