package org.webpieces.httpcommon.api;

import java.net.InetSocketAddress;

import org.webpieces.httpcommon.impl.Http2ClientEngineImpl;
import org.webpieces.httpcommon.impl.Http2ServerEngineImpl;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.hpack.api.HpackParser;

public class Http2EngineFactory {
    static public Http2ServerEngine createHttp2ServerEngine(
    		HpackParser http2Parser,
        Channel channel,
        InetSocketAddress addr,
        Http2SettingsMap http2SettingsMap) {
        return new Http2ServerEngineImpl(http2Parser, channel, addr, http2SettingsMap);
    }

    static public Http2ClientEngine createHttp2ClientEngine(
    		HpackParser http2Parser,
        Channel channel,
        InetSocketAddress addr, Http2SettingsMap http2SettingsMap) {
        return new Http2ClientEngineImpl(http2Parser, channel, addr, http2SettingsMap);
    }
}
