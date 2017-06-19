package org.webpieces.httpclient11.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

public class HttpClientImpl implements HttpClient {

	private ChannelManager mgr;
	private HttpParser parser;

	public HttpClientImpl(ChannelManager mgr, HttpParser parser) {
		this.mgr = mgr;
		this.parser = parser;
	}

	@Override
	public HttpSocket createHttpSocket(String idForLogging) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new HttpSocketImpl(channel, parser);
	}
	
	@Override
	public HttpSocket createHttpsSocket(String idForLogging, SSLEngine engine) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new HttpSocketImpl(channel, parser);
	}

}
