package org.webpieces.asyncserver.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.webpieces.asyncserver.impl.AsyncServerManagerImpl;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

public class AsyncServerMgrFactory {

	private static int counter = 0;
	
	public static synchronized int getCount() {
		return counter++;
	}
	
	public static AsyncServerManager createAsyncServer(String id, BufferPool pool) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr(id, pool, executor);
		return createAsyncServer(mgr);
	}
	
	public static AsyncServerManager createAsyncServer(ChannelManager channelManager) {
		return new AsyncServerManagerImpl(channelManager);
	}
	
}
