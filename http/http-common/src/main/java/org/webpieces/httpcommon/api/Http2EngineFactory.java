package org.webpieces.httpcommon.api;

import com.webpieces.http2parser.api.Http2Parser;
import org.webpieces.httpcommon.impl.Http2ClientEngineImpl;
import org.webpieces.httpcommon.impl.Http2EngineImpl;
import org.webpieces.httpcommon.impl.Http2ServerEngineImpl;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

import java.net.InetSocketAddress;

public class Http2EngineFactory {
    static public Http2ServerEngine createHttp2ServerEngine(Http2Parser http2Parser, Channel channel, InetSocketAddress addr) {
        return new Http2ServerEngineImpl(http2Parser, channel, addr);
    }

    static public Http2ClientEngine createHttp2ClientEngine(Http2Parser http2Parser, Channel channel, InetSocketAddress addr) {
        return new Http2ClientEngineImpl(http2Parser, channel, addr);
    }
}
