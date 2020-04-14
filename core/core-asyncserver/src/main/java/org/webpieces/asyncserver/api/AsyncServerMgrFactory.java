package org.webpieces.asyncserver.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.asyncserver.impl.AsyncServerManagerImpl;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

import io.micrometer.core.instrument.MeterRegistry;
import org.webpieces.util.threading.MonitorThreadPool;

public class AsyncServerMgrFactory {

	private static AtomicInteger counter = new AtomicInteger(0);
	
	public static int getCount() {
		return counter.getAndIncrement();
	}
	
	public static AsyncServerManager createAsyncServer(String id, BufferPool pool, BackpressureConfig config, MeterRegistry metrics) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		MonitorThreadPool.monitor(metrics, executor, id);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(metrics);
		ChannelManager mgr = factory.createMultiThreadedChanMgr(id, pool, config, executor);
		return createAsyncServer(mgr, metrics);
	}
	
	public static AsyncServerManager createAsyncServer(ChannelManager channelManager, MeterRegistry metrics) {
		return new AsyncServerManagerImpl(channelManager, metrics);
	}
	
}
