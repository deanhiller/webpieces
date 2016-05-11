package org.webpieces.netty.impl;

import org.webpieces.netty.api.BufferPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;

public class NettyManager implements ChannelManager {

	private BufferPool pool;

	public NettyManager(BufferPool pool) {
		this.pool = pool;
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connListener) {
		return new NettyServerChannel();
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		return new NettyTCPChannel(pool);
	}

	@Override
	public UDPChannel createUDPChannel(String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop() {
	}

}
