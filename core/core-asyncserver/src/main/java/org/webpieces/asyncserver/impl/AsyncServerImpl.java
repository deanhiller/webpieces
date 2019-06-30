package org.webpieces.asyncserver.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class AsyncServerImpl implements AsyncServer {

	private static final Logger log = LoggerFactory.getLogger(AsyncServerImpl.class);

	private TCPServerChannel serverChannel;
	private DefaultConnectionListener connectionListener;
	private SSLEngineFactory sslFactory;

	public AsyncServerImpl(TCPServerChannel serverChannel2, DefaultConnectionListener connectionListener,
			ProxyDataListener proxyListener, SSLEngineFactory sslFactory) {
		this.serverChannel = serverChannel2;
		this.connectionListener = connectionListener;
		this.sslFactory = sslFactory;
	}

	@Override
	public CompletableFuture<Void> start(SocketAddress bindAddr) {
		log.info("binding server socket="+bindAddr+" with your engine="+sslFactory+"(if null, ssl is off)");
		CompletableFuture<Void> future = serverChannel.bind(bindAddr);
		return future.thenApply(v -> {
			InetSocketAddress localAddr = getUnderlyingChannel().getLocalAddress();
			log.info("now listening for incoming requests on "+localAddr);
			return null;
		});
	}
	
	@Override
	public CompletableFuture<Void> closeServerChannel() {
		serverChannel.closeServerChannel();
		
		return connectionListener.closeChannels();
	}
	
	@Override
	public void enableOverloadMode(ByteBuffer overloadResponse) {
		connectionListener.enableOverloadMode(overloadResponse);
	}

	@Override
	public void disableOverloadMode() {
		connectionListener.disableOverloadMode();
	}

	@Override
	public TCPServerChannel getUnderlyingChannel() {
		return serverChannel;
	}

}
