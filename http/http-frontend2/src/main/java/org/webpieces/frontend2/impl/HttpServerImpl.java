package org.webpieces.frontend2.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class HttpServerImpl implements HttpServer {

	private static final Logger log = LoggerFactory.getLogger(HttpServerImpl.class);
	private AsyncServer server;
	private FrontendConfig config;
	private Layer1ServerListener listener;

	public HttpServerImpl(AsyncServer server, FrontendConfig config, Layer1ServerListener listener) {
		this.server = server;
		this.config = config;
		this.listener = listener;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		log.info("starting to listen to port="+config.bindAddress);
		server.start(config.bindAddress);
		InetSocketAddress localAddr = server.getUnderlyingChannel().getLocalAddress();
		listener.setSvrSocketAddr(localAddr);
		log.info("now listening for incoming requests on "+localAddr);
	}
	
	@Override
	public CompletableFuture<Void> close() {
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
