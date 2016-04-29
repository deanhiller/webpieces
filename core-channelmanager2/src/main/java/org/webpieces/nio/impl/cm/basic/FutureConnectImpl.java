package org.webpieces.nio.impl.cm.basic;

import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;


public class FutureConnectImpl implements ConnectionListener {

	private CompletableFuture<Channel> promise;
	private DataListener listener;

	public FutureConnectImpl(CompletableFuture<Channel> promise, DataListener listener) {
		this.promise = promise;
		this.listener = listener;
	}
	
	@Override
	public DataListener connected(Channel channel) {
		promise.complete(channel);
		return listener;
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		promise.completeExceptionally(e);
	}

}
