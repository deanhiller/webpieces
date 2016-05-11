package org.webpieces.nio.impl.ssl;

import java.util.function.Function;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.DatagramListener;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SslListener;
import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.data.api.BufferPool;

public class SslChannelService implements ChannelManager {

	private ChannelManager mgr;
	private AsyncSSLFactory factory;
	private BufferPool pool;

	public SslChannelService(ChannelManager mgr, BufferPool pool, AsyncSSLFactory factory) {
		this.mgr = mgr;
		this.pool = pool;
		this.factory = factory;
	}

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener,
			DataListener dataListener) {
			throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public TCPChannel createTCPChannel(String id, DataListener listener) {
		SSLEngine engine = null;
		SslTryCatchListener wrapListener = new SslTryCatchListener(listener);
		
		Function<SslListener, AsyncSSLEngine> function = l -> AsyncSSLFactory.createParser(id, engine, pool, l);
		
		SslTCPChannel sslChannel = new SslTCPChannel(function, wrapListener);
		TCPChannel channel = mgr.createTCPChannel(id, sslChannel.getDataListener());
		sslChannel.init(channel);
		return sslChannel;
	}

	@Override
	public UDPChannel createUDPChannel(String id, DataListener listener) {
		return mgr.createUDPChannel(id, listener);
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
