package org.webpieces.netty.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

public class MyFutureAdaptor implements GenericFutureListener<ChannelFuture> {

	private CompletableFuture<Channel> promise;
	private NettyTCPChannel channel;

	public MyFutureAdaptor(CompletableFuture<Channel> promise, NettyTCPChannel nettyTCPChannel) {
		this.promise = promise;
		this.channel = nettyTCPChannel;
	}

	@Override
    public void operationComplete(ChannelFuture future) throws Exception {
		if(future.isSuccess()) {
			promise.complete(channel);
			channel.setChannelImpl(future.channel());
		}
		
		try {
			future.syncUninterruptibly();
		} catch(Exception e) {
			promise.completeExceptionally(e);
		}
		
	}
}
