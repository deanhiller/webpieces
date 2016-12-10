package org.webpieces.httpcommon.api;

import java.net.InetSocketAddress;

import org.webpieces.httpcommon.impl.Http2ClientEngineImpl;
import org.webpieces.httpcommon.impl.Http2ServerEngineImpl;
import org.webpieces.nio.api.channels.Channel;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2SettingsMap;

public class Http2EngineFactory {
    static public Http2ServerEngine createHttp2ServerEngine(
        Http2Parser http2Parser,
        Channel channel,
        InetSocketAddress addr,
        Http2SettingsMap http2SettingsMap) {
        return new Http2ServerEngineImpl(http2Parser, channel, addr, http2SettingsMap);
    }

    static public Http2ClientEngine createHttp2ClientEngine(
        Http2Parser http2Parser,
        Channel channel,
        InetSocketAddress addr, Http2SettingsMap http2SettingsMap) {
        return new Http2ClientEngineImpl(http2Parser, channel, addr, http2SettingsMap);
    }
}
