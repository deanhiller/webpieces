package com.webpieces.hpack.api;

import org.webpieces.data.api.BufferPool;

import com.webpieces.hpack.api.subparsers.HeaderPriorityParser;
import com.webpieces.hpack.impl.HpackParserImpl;
import com.webpieces.hpack.impl.HpackStatefulParserImpl;
import com.webpieces.hpack.impl.subparsers.HeaderPriorityParserImpl;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

public class HpackParserFactory {

    public static HpackParser createParser(BufferPool bufferPool, boolean ignoreUnknownFrames) {
    	Http2Parser parser = Http2ParserFactory.createParser(bufferPool);
        return new HpackParserImpl(parser, ignoreUnknownFrames);
    }

    public static HpackStatefulParser createStatefulParser(BufferPool bufferPool, HpackConfig config) {
    	HpackParser parser = createParser(bufferPool, config.isIgnoreUnknownFrames());
    	return new HpackStatefulParserImpl(parser, config);
    }
    
	public static HeaderPriorityParser createHeaderParser() {
		return new HeaderPriorityParserImpl();
	}
}
