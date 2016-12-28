package org.webpieces.httpclient.impl2;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient.api2.HttpClient;
import org.webpieces.httpclient.api2.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2SettingsMap;

public class HttpClient2Impl implements HttpClient {

	private ChannelManager mgr;
	private HttpParser httpParser;
	private Http2Parser http2Parser;
	private Http2SettingsMap http2SettingsMap;
	private boolean forceHttp2;

	public HttpClient2Impl(
			ChannelManager mgr,
			HttpParser httpParser,
			Http2Parser http2Parser, 
			Http2SettingsMap http2SettingsMap,
			boolean forceHttp2
	) {
		this.mgr = mgr;
		this.httpParser = httpParser;
		this.http2Parser = http2Parser;
		this.http2SettingsMap = http2SettingsMap;
		this.forceHttp2 = forceHttp2;
	}

	@Override
	public HttpSocket createHttpSocket(String idForLogging) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new HttpSocketImpl(channel, httpParser, http2Parser, forceHttp2);
	}

	@Override
	public HttpSocket createHttpsSocket(String idForLogging, SSLEngine engine) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new HttpSocketImpl(channel, httpParser, http2Parser, forceHttp2);
	}

}
