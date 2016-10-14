package org.webpieces.frontend.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class HttpServerSocketImpl implements HttpServerSocket {

	private AsyncServer server;
	private AsyncDataListener dataListener;

	public HttpServerSocketImpl(TimedListener requestListener, HttpParser parser, FrontendConfig config, boolean isHttps) {
		Http11Layer http11Layer = new Http11Layer(parser, requestListener, config, isHttps);
		dataListener = new Http11DataListener(http11Layer);
	}
	
	public void init(AsyncServer asyncServer) {
		this.server = asyncServer;
	}
	
	@Override
	public CompletableFuture<Void> closeSocket() {
		return server.closeServerChannel();
	}
	
	AsyncDataListener getDataListener() {
		return dataListener;
	}

	@Override
	public void enableOverloadMode(ByteBuffer overloadResponse) {
		server.enableOverloadMode(overloadResponse);
	}

	@Override
	public void disableOverloadMode() {
		server.disableOverloadMode();
	}

	@Override
	public TCPServerChannel getUnderlyingChannel() {
		return server.getUnderlyingChannel();
	}

}
