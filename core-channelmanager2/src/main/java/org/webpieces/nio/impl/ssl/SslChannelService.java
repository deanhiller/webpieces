package org.webpieces.nio.impl.ssl;

import java.util.function.Function;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SslListener;

public class SslChannelService implements ChannelManager {

	private ChannelManager mgr;
	private BufferPool pool;

	public SslChannelService(ChannelManager mgr, BufferPool pool) {
		this.mgr = mgr;
		this.pool = pool;
	}

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener) {
		return mgr.createTCPServerChannel(id, connectionListener);
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener, SSLEngineFactory factory) {
		if(factory == null || connectionListener == null || id == null)
			throw new IllegalArgumentException("no arguments can be null");
		ConnectionListener wrapperConnectionListener = new SslConnectionListener(connectionListener, pool, factory);
		//because no methods return futures in this type of class, we do not need to proxy him....
		return mgr.createTCPServerChannel(id, wrapperConnectionListener);
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		return mgr.createTCPChannel(id);
	}
	
	@Override
	public TCPChannel createTCPChannel(String id, SSLEngine engine) {
		if(engine == null || id == null)
			throw new IllegalArgumentException("no arguments can be null");
		Function<SslListener, AsyncSSLEngine> function = l -> AsyncSSLFactory.create(id, engine, pool, l);
		
		TCPChannel channel = mgr.createTCPChannel(id);
		SslTCPChannel sslChannel = new SslTCPChannel(function, channel);
		return sslChannel;
	}

	@Override
	public UDPChannel createUDPChannel(String id) {
		return mgr.createUDPChannel(id);
	}

	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener) {
		return mgr.createDatagramChannel(id, bufferSize, listener);
	}

	@Override
	public void stop() {
		mgr.stop();
	}

}
