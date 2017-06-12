package org.webpieces.asyncserver.impl;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class AsyncServerImpl implements AsyncServer {

	private TCPServerChannel serverChannel;
	private DefaultConnectionListener connectionListener;

	public AsyncServerImpl(TCPServerChannel serverChannel2, DefaultConnectionListener connectionListener,
			ProxyDataListener proxyListener) {
		this.serverChannel = serverChannel2;
		this.connectionListener = connectionListener;
	}

	@Override
	public CompletableFuture<Void> start(SocketAddress bindAddr) {
		return serverChannel.bind(bindAddr);
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
