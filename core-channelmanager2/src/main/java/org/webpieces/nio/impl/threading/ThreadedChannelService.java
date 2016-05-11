package org.webpieces.nio.impl.threading;

import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;
import org.webpieces.util.threading.SessionExecutor;
import org.webpieces.util.threading.SessionExecutorImpl;

public class ThreadedChannelService implements ChannelManager {

	private ChannelManager mgr;
	private SessionExecutor executor;

	public ThreadedChannelService(ChannelManager mgr, Executor executor) {
		this.mgr = mgr;
		this.executor = new SessionExecutorImpl(executor);
	}

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener) {
		ConnectionListener wrapperConnectionListener = new ThreadConnectionListener(connectionListener, executor);
		//because no methods return futures in this type of class, we do not need to proxy him....
		return mgr.createTCPServerChannel(id, wrapperConnectionListener);
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		TCPChannel channel = mgr.createTCPChannel(id);
		return new ThreadTCPChannel(channel, executor);
	}

	@Override
	public UDPChannel createUDPChannel(String id) {
		UDPChannel channel = mgr.createUDPChannel(id);
		return new ThreadUDPChannel(channel, executor);
	}

	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener) {
		return mgr.createDatagramChannel(id, bufferSize, new ThreadDatagramListener(listener, executor));
	}

	@Override
	public void stop() {
		mgr.stop();
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

}
