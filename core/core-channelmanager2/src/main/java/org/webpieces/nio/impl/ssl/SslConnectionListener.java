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
	private boolean isStartInPlainText;

	public SslConnectionListener(ConnectionListener connectionListener, BufferPool pool, SSLEngineFactory sslFactory, SSLMetrics metrics, boolean isStartInPlainText) {
		this.connectionListener = connectionListener;
		this.pool = pool;
		this.sslFactory = sslFactory;
		this.metrics = metrics;
		this.isStartInPlainText = isStartInPlainText;
	}

	//thanks to SessionExecutor we will not start getting data to the listener until connected returns
	//control back to the thread
	@Override
	public CompletableFuture<DataListener> connected(Channel c, boolean isReadyForWrites) {
		TCPChannel realChannel = (TCPChannel) c;
		SslTCPChannel sslChannel = new SslTCPChannel(pool, realChannel, connectionListener, sslFactory, metrics, isStartInPlainText);
		CompletableFuture<DataListener> plainTextListener = null;
		if(isStartInPlainText)
			plainTextListener = connectionListener.connected(sslChannel, true); //connected right away
		else
			plainTextListener = connectionListener.connected(sslChannel, false); //SSL Handshake in process so NOT fully connected
		
		return plainTextListener.thenApply( plainListener -> {
			return sslChannel.getSocketDataListener(plainListener);
		});
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
	}

}
