package org.webpieces.asyncserver.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.asyncserver.impl.AsyncServerManagerImpl;
import org.webpieces.nio.api.BufferCreationPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

public class AsyncServerMgrFactory {

	public static AsyncServerManager createAsyncServer(String id, BufferCreationPool pool) {
		Executor executor = Executors.newFixedThreadPool(1);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createChannelManager(id, pool, executor);
		return createAsyncServer(mgr);
	}
	
	public static AsyncServerManager createAsyncServer(ChannelManager channelManager) {
		return new AsyncServerManagerImpl(channelManager);
	}
	
}
