package org.webpieces.nio.impl.cm.basic;

import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;


public class FutureConnectImpl implements ConnectionListener {

	private CompletableFuture<Channel> promise;

	public FutureConnectImpl(CompletableFuture<Channel> promise) {
		this.promise = promise;
	}
	
	@Override
	public CompletableFuture<DataListener> connected(Channel channel, boolean s) {
		promise.complete(channel);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		promise.completeExceptionally(e);
	}

}
