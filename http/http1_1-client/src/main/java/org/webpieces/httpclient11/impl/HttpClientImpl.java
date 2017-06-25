package org.webpieces.httpclient11.impl;

import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

public class HttpClientImpl implements HttpClient {

	private ChannelManager mgr;
	private HttpParser parser;
	private String id;
	private AtomicInteger counter = new AtomicInteger();
	private AtomicInteger httpsCounter = new AtomicInteger();

	public HttpClientImpl(String id, ChannelManager mgr, HttpParser parser) {
		this.id = id;
		this.mgr = mgr;
		this.parser = parser;
	}

	@Override
	public HttpSocket createHttpSocket() {
		int count = counter.getAndIncrement();
		String idForLogging = id+count+"Http";
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new HttpSocketImpl(new Proxy(channel), parser);
	}
	
	@Override
	public HttpSocket createHttpsSocket(SSLEngine engine) {
		int count = httpsCounter.getAndIncrement();
		String idForLogging = id+count+"Https";
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new HttpSocketImpl(new Proxy(channel), parser);
	}

}
