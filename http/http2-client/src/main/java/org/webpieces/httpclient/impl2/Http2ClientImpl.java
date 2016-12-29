package org.webpieces.httpclient.impl2;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient.api.Http2Client;
import org.webpieces.httpclient.api.Http2Socket;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2parser.api.Http2Parser;

public class Http2ClientImpl implements Http2Client {

	private ChannelManager mgr;
	private Http2Parser http2Parser;

	public Http2ClientImpl(
			ChannelManager mgr,
			Http2Parser http2Parser 
	) {
		this.mgr = mgr;
		this.http2Parser = http2Parser;
	}

	@Override
	public Http2Socket createHttpSocket(String idForLogging) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging);
		return new Http2SocketImpl(channel, http2Parser);
	}

	@Override
	public Http2Socket createHttpsSocket(String idForLogging, SSLEngine engine) {
		TCPChannel channel = mgr.createTCPChannel(idForLogging, engine);
		return new Http2SocketImpl(channel, http2Parser);
	}

}
