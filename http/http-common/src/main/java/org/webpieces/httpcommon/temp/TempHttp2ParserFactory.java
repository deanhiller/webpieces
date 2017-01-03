package org.webpieces.httpcommon.temp;

import org.webpieces.data.api.BufferPool;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

public class TempHttp2ParserFactory {

    public static TempHttp2Parser createParser(BufferPool bufferPool) {
    	Http2Parser parser = Http2ParserFactory.createParser(bufferPool);
        return new TempHttp2ParserImpl(parser);
    }
}
