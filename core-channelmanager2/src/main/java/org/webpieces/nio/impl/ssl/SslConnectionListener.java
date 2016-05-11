package org.webpieces.nio.impl.ssl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SslListener;

import com.webpieces.data.api.BufferPool;

public class SslConnectionListener implements ConnectionListener {

	private ConnectionListener connectionListener;
	private BufferPool pool;

	public SslConnectionListener(ConnectionListener connectionListener, BufferPool pool) {
		this.connectionListener = connectionListener;
		this.pool = pool;
	}

	//thanks to SessionExecutor we will not start getting data to the listener until connected returns
	//control back to the thread
	@Override
	public CompletableFuture<DataListener> connected(Channel c, boolean s) {
		TCPChannel realChannel = (TCPChannel) c;
		SSLEngine engine = null;		
		Function<SslListener, AsyncSSLEngine> function = l -> AsyncSSLFactory.createParser(c+"", engine, pool, l);
		SslTCPChannel sslChannel = new SslTCPChannel(function, realChannel);
		
		CompletableFuture<DataListener> listenerFuture = connectionListener.connected(sslChannel, true);
		return listenerFuture.thenApply(l -> sslChannel.accept(l));
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
	}

}
