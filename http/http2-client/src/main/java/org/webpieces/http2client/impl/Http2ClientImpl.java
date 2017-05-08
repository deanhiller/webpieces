package org.webpieces.http2client.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;
import com.webpieces.http2engine.api.client.Http2Config;

public class Http2ClientImpl implements Http2Client {

	private ChannelManager mgr;
	private HpackParser http2Parser;
	private Http2ClientEngineFactory factory;
	private Http2Config config;
	private SessionExecutor sessionExecutor;

	public Http2ClientImpl(
			Http2Config config, 
			ChannelManager mgr,
			HpackParser http2Parser,
			Http2ClientEngineFactory factory,
			SessionExecutor sessionExecutor
	) {
		this.config = config;
		this.mgr = mgr;
		this.http2Parser = http2Parser;
		this.factory = factory;
		this.sessionExecutor = sessionExecutor;
	}

	@Override
	public Http2Socket createHttpSocket(String idForLogging) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new Http2SocketImpl(config, channel, http2Parser, factory, sessionExecutor);
	}

	@Override
	public Http2Socket createHttpsSocket(String idForLogging, SSLEngine engine) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new Http2SocketImpl(config, channel, http2Parser, factory, sessionExecutor);
	}

}
