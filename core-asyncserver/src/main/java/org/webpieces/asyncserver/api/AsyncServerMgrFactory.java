package org.webpieces.asyncserver.api;

import org.webpieces.asyncserver.impl.AsyncServerManagerImpl;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

import com.webpieces.data.api.BufferPool;

public class AsyncServerMgrFactory {

	private static int counter = 0;
	
	public static synchronized int getCount() {
		return counter++;
	}
	
	public static AsyncServerManager createAsyncServer(String id, BufferPool pool) {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createChannelManager(id, pool);
		return createAsyncServer(mgr);
	}
	
	public static AsyncServerManager createAsyncServer(ChannelManager channelManager) {
		return new AsyncServerManagerImpl(channelManager);
	}
	
}
