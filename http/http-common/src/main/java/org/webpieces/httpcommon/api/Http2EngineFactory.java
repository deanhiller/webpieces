package org.webpieces.httpcommon.api;

import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.httpcommon.impl.Http2EngineImpl;
import org.webpieces.nio.api.channels.TCPChannel;

import java.net.InetSocketAddress;

public class Http2EngineFactory {
    static public Http2Engine createHttp2Engine(Http2Parser http2Parser, TCPChannel channel, InetSocketAddress addr, Http2EngineImpl.HttpSide side) {
        return new Http2EngineImpl(http2Parser, channel, addr, side);
    }
}
