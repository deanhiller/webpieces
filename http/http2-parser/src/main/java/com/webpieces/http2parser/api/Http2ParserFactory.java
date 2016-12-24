package com.webpieces.http2parser.api;


import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;

import com.webpieces.http2parser.impl.Http2ParserImpl;
import com.webpieces.http2parser2.impl.Http2Parser2Impl;

public class Http2ParserFactory {
    public static Http2Parser createParser(BufferPool bufferPool) {
        return new Http2ParserImpl(bufferPool);
    }

	public static Http2Parser2 createParser2(BufferCreationPool bufferCreationPool) {
		return new Http2Parser2Impl(bufferCreationPool);
	}
}
