package org.webpieces.netty.impl;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.util.futures.PromiseImpl;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

public class MyFutureAdaptor implements GenericFutureListener<ChannelFuture> {

	private PromiseImpl<Channel, FailureInfo> promise;
	private NettyTCPChannel channel;

	public MyFutureAdaptor(PromiseImpl<Channel, FailureInfo> promise, NettyTCPChannel nettyTCPChannel) {
		this.promise = promise;
		this.channel = nettyTCPChannel;
	}

	@Override
    public void operationComplete(ChannelFuture future) throws Exception {
		if(future.isSuccess()) {
			promise.setResult(channel);
			channel.setChannelImpl(future.channel());
		}
		
		try {
			future.syncUninterruptibly();
		} catch(Exception e) {
			promise.setFailure(new FailureInfo(channel, e));
		}
		
	}
}
