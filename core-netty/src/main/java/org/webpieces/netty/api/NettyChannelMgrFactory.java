package org.webpieces.netty.api;

import org.webpieces.netty.impl.NettyChannelMgrFactoryImpl;
import org.webpieces.nio.api.ChannelManager;

public abstract class NettyChannelMgrFactory {

	public static NettyChannelMgrFactory createFactory() {
		return new NettyChannelMgrFactoryImpl();
	}
	
	public abstract ChannelManager createChannelManager(BufferPool pool);
}
