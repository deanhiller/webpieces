package com.webpieces.http2parser.api;


import org.webpieces.data.api.BufferPool;

import com.webpieces.http2parser.impl.Http2Parser2Impl;
import com.webpieces.http2parser.impl.HttpStatefulParserImpl;

public class Http2ParserFactory {
	
	public static Http2Parser createParser(BufferPool bufferCreationPool) {
		return new Http2Parser2Impl(bufferCreationPool);
	}
	
	public static Http2StatefulParser createStatefulParser(Http2Parser statelessParser) {
		return new HttpStatefulParserImpl(statelessParser);
	}
}
