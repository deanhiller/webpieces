package org.webpieces.netty.api;

import java.util.concurrent.Executor;

import org.webpieces.netty.impl.NettyChannelMgrFactoryImpl;
import org.webpieces.nio.api.ChannelManager;

public abstract class NettyChannelMgrFactory {

	public static NettyChannelMgrFactory createFactory() {
		return new NettyChannelMgrFactoryImpl();
	}
	
	public abstract ChannelManager createChannelManager(Executor promiseExecutor, BufferPool pool);
}
