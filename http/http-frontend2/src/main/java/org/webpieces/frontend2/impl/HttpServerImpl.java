package org.webpieces.frontend2.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class HttpServerImpl implements HttpServer {

	private AsyncServer server;
	private HttpSvrConfig config;
	private Layer1ServerListener listener;
	private boolean started;

	public HttpServerImpl(AsyncServer server, HttpSvrConfig config, Layer1ServerListener listener) {
		this.server = server;
		this.config = config;
		this.listener = listener;
	}
	
	@Override
	public XFuture<Void> start() {
		XFuture<Void> future = server.start(config.bindAddress);
		return future.thenApply(v -> {
			InetSocketAddress localAddr = server.getUnderlyingChannel().getLocalAddress();
			listener.setSvrSocketAddr(localAddr);
			started = true;
			return null;
		});
	}

	@Override
	public XFuture<Void> close() {
		if(!started)
			throw new IllegalArgumentException("The server was not fully started yet.  you cannot close it until id is started");
		return server.closeServerChannel();
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
