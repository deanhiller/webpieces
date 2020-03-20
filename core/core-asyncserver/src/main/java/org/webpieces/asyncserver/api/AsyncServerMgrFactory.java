package org.webpieces.asyncserver.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.asyncserver.impl.AsyncServerManagerImpl;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

import org.webpieces.util.metrics.MetricStrategy;

public class AsyncServerMgrFactory {

	private static AtomicInteger counter = new AtomicInteger(0);
	
	public static int getCount() {
		return counter.getAndIncrement();
	}
	
	public static AsyncServerManager createAsyncServer(String id, BufferPool pool, BackpressureConfig config) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		MetricStrategy.monitorExecutor(executor, id);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr(id, pool, config, executor);
		return createAsyncServer(mgr);
	}
	
	public static AsyncServerManager createAsyncServer(ChannelManager channelManager) {
		return new AsyncServerManagerImpl(channelManager);
	}
	
}
