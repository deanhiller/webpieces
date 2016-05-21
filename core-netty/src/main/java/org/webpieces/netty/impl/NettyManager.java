package org.webpieces.netty.impl;

import javax.net.ssl.SSLEngine;

import org.webpieces.netty.api.BufferPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.SSLEngineProxy;
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


	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		throw new UnsupportedOperationException("SSL not supported at this level.");
	}

	@Override
	public TCPChannel createTCPChannel(String id, SSLEngine engine) {
		throw new UnsupportedOperationException("SSL not supported at this level.");
	}

	@Override
	public TCPChannel createTCPChannel(String id, SSLEngineProxy engine) {
		// TODO Auto-generated method stub
		return null;
	}


}
