package org.webpieces.frontend2.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class HttpServerImpl implements HttpServer {

	private static final Logger log = LoggerFactory.getLogger(HttpServerImpl.class);
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
	public CompletableFuture<Void> start() {
		
		log.info("starting to listen to port="+config.bindAddress);
		CompletableFuture<Void> future = server.start(config.bindAddress);
		return future.thenApply(v -> {
			InetSocketAddress localAddr = server.getUnderlyingChannel().getLocalAddress();
			listener.setSvrSocketAddr(localAddr);
			started = true;
			log.info("now listening for incoming requests on "+localAddr);
			return null;
		});
	}

	@Override
	public CompletableFuture<Void> close() {
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
