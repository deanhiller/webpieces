package org.webpieces.netty.impl;

import org.webpieces.netty.api.BufferPool;
import org.webpieces.netty.api.NettyChannelMgrFactory;
import org.webpieces.nio.api.ChannelManager;

public class NettyChannelMgrFactoryImpl extends NettyChannelMgrFactory {

	@Override
	public ChannelManager createChannelManager(BufferPool pool) {
		return new NettyManager(pool);
	}

}
