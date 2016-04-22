package org.webpieces.netty.impl;

import java.util.concurrent.Executor;

import org.webpieces.netty.api.BufferPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;

public class NettyManager implements ChannelManager {

	private Executor executor;
	private BufferPool pool;

	public NettyManager(Executor executor, BufferPool pool) {
		this.executor = executor;
		this.pool = pool;
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id) {
		return new NettyServerChannel();
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		return new NettyTCPChannel(executor, pool);
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
