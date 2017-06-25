package org.webpieces.http2client.impl;

import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;

public class Http2ClientImpl implements Http2Client {

	private ChannelManager mgr;
	private Http2ClientEngineFactory factory;
	private AtomicInteger counter = new AtomicInteger();
	private AtomicInteger httpsCounter = new AtomicInteger();
	private String id;
	
	public Http2ClientImpl(
			String id,
			ChannelManager mgr,
			Http2ClientEngineFactory factory
	) {
		this.id = id;
		this.mgr = mgr;
		this.factory = factory;
	}

	@Override
	public Http2Socket createHttpSocket() {
		int count = counter.getAndIncrement();
		String idForLogging = id+count+"Http2";
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new Http2SocketImpl(channel, factory);
	}

	@Override
	public Http2Socket createHttpsSocket(SSLEngine engine) {
		int count = httpsCounter.getAndIncrement();
		String idForLogging = id+count+"Https2";
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new Http2SocketImpl(channel, factory);
	}

}
