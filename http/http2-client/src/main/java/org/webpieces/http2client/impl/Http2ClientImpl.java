package org.webpieces.http2client.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2engine.api.Http2EngineFactory;
import com.webpieces.http2parser.api.Http2Parser2;

public class Http2ClientImpl implements Http2Client {

	private ChannelManager mgr;
	private Http2Parser2 http2Parser;
	private Http2EngineFactory factory;

	public Http2ClientImpl(
			ChannelManager mgr,
			Http2Parser2 http2Parser,
			Http2EngineFactory factory
	) {
		this.mgr = mgr;
		this.http2Parser = http2Parser;
		this.factory = factory;
	}

	@Override
	public Http2Socket createHttpSocket(String idForLogging) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new Http2SocketImpl(channel, http2Parser, factory);
	}

	@Override
	public Http2Socket createHttpsSocket(String idForLogging, SSLEngine engine) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new Http2SocketImpl(channel, http2Parser, factory);
	}

}
