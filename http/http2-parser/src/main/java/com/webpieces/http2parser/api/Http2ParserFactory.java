package com.webpieces.http2parser.api;


import com.webpieces.http2parser.impl.Http2ParserImpl;
import org.webpieces.data.api.BufferPool;

public class Http2ParserFactory {
    public static Http2Parser createParser(BufferPool bufferPool) {
        return new Http2ParserImpl(bufferPool);
    }
}
