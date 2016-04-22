package org.webpieces.netty.impl;

import java.util.concurrent.Executor;

import org.webpieces.netty.api.BufferPool;
import org.webpieces.netty.api.NettyChannelMgrFactory;
import org.webpieces.nio.api.ChannelManager;

public class NettyChannelMgrFactoryImpl extends NettyChannelMgrFactory {

	@Override
	public ChannelManager createChannelManager(Executor promiseExecutor, BufferPool pool) {
		return new NettyManager(promiseExecutor, pool);
	}

}
