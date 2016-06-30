package org.webpieces.nio.impl.ssl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class SslConnectionListener implements ConnectionListener {

	private ConnectionListener connectionListener;
	private BufferPool pool;
	private SSLEngineFactory sslFactory;

	public SslConnectionListener(ConnectionListener connectionListener, BufferPool pool, SSLEngineFactory sslFactory) {
		this.connectionListener = connectionListener;
		this.pool = pool;
		this.sslFactory = sslFactory;
	}

	//thanks to SessionExecutor we will not start getting data to the listener until connected returns
	//control back to the thread
	@Override
	public CompletableFuture<DataListener> connected(Channel c, boolean s) {
		TCPChannel realChannel = (TCPChannel) c;
		SslTCPChannel sslChannel = new SslTCPChannel(pool, realChannel, connectionListener, sslFactory);
		return CompletableFuture.completedFuture(sslChannel.getSocketDataListener());
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
	}

}
