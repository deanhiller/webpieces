package org.webpieces.httpcommon.temp;

import org.webpieces.data.api.BufferPool;

import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;

public class TempHttp2ParserFactory {

    public static TempHttp2Parser createParser(BufferPool bufferPool) {
    	Http2Parser2 parser = Http2ParserFactory.createParser2(bufferPool);
        return new TempHttp2ParserImpl(parser);
    }
}
