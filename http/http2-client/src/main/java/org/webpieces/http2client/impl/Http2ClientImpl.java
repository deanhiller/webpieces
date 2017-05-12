package org.webpieces.http2client.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;

public class Http2ClientImpl implements Http2Client {

	private ChannelManager mgr;
	private Http2ClientEngineFactory factory;

	public Http2ClientImpl(
			ChannelManager mgr,
			Http2ClientEngineFactory factory
	) {
		this.mgr = mgr;
		this.factory = factory;
	}

	@Override
	public Http2Socket createHttpSocket(String idForLogging) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new Http2SocketImpl(channel, factory);
	}

	@Override
	public Http2Socket createHttpsSocket(String idForLogging, SSLEngine engine) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new Http2SocketImpl(channel, factory);
	}

}
