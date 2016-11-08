package org.webpieces.nio.impl.ssl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

class SslConnectionListener implements ConnectionListener {

	private ConnectionListener connectionListener;
	private BufferPool pool;
	private SSLEngineFactory sslFactory;
	private List<String> supportedAlpnProtocols;

	public SslConnectionListener(ConnectionListener connectionListener, BufferPool pool, SSLEngineFactory sslFactory, List<String> supportedAlpnProtocols) {
		this.connectionListener = connectionListener;
		this.pool = pool;
		this.sslFactory = sslFactory;
		this.supportedAlpnProtocols = supportedAlpnProtocols;
	}

	//thanks to SessionExecutor we will not start getting data to the listener until connected returns
	//control back to the thread
	@Override
	public CompletableFuture<DataListener> connected(Channel c, boolean isReadyForWrites) {
		TCPChannel realChannel = (TCPChannel) c;
		SslTCPChannel sslChannel = new SslTCPChannel(pool, realChannel, connectionListener, sslFactory, supportedAlpnProtocols);
		connectionListener.connected(sslChannel, false);
		return CompletableFuture.completedFuture(sslChannel.getSocketDataListener());
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
	}

}
