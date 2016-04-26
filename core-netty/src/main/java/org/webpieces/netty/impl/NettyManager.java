package org.webpieces.netty.impl;

import org.webpieces.netty.api.BufferPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;

public class NettyManager implements ChannelManager {

	private BufferPool pool;

	public NettyManager(BufferPool pool) {
		this.pool = pool;
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id) {
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
	public DatagramChannel createDatagramChannel(String id, int bufferSize) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop() {
	}

}
