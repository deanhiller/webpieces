package com.webpieces.http2parser.api;


import org.webpieces.data.api.BufferPool;

import com.webpieces.http2parser.impl.Http2ParserImpl;
import com.webpieces.http2parser.impl.Http2StatefulParserImpl;

public class Http2ParserFactory {
	
	public static Http2Parser createParser(BufferPool bufferCreationPool) {
		return new Http2ParserImpl(bufferCreationPool);
	}
	
	public static Http2StatefulParser createStatefulParser(BufferPool pool, long maxFrameSize) {
		Http2Parser statelessParser = createParser(pool);
		return new Http2StatefulParserImpl(statelessParser, maxFrameSize);
	}
}
