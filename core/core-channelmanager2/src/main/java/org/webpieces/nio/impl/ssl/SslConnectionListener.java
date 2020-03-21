package org.webpieces.nio.impl.ssl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.ssl.api.SSLMetrics;

public class SslConnectionListener implements ConnectionListener {

	private ConnectionListener connectionListener;
	private BufferPool pool;
	private SSLEngineFactory sslFactory;
	private SSLMetrics metrics;

	public SslConnectionListener(ConnectionListener connectionListener, BufferPool pool, SSLEngineFactory sslFactory, SSLMetrics metrics) {
		this.connectionListener = connectionListener;
		this.pool = pool;
		this.sslFactory = sslFactory;
		this.metrics = metrics;
	}

	//thanks to SessionExecutor we will not start getting data to the listener until connected returns
	//control back to the thread
	@Override
	public CompletableFuture<DataListener> connected(Channel c, boolean isReadyForWrites) {
		TCPChannel realChannel = (TCPChannel) c;
		SslTCPChannel sslChannel = new SslTCPChannel(pool, realChannel, connectionListener, sslFactory, metrics);
		connectionListener.connected(sslChannel, false);
		return CompletableFuture.completedFuture(sslChannel.getSocketDataListener());
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
	}

}
