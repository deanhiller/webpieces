package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.mocks.MockAsyncListener;
import org.webpieces.nio.api.mocks.MockSvrSideJdkChannel;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.nio.api.mocks.MockSvrChannel;
import org.webpieces.util.threading.DirectExecutor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

public class TestSvrReading {

	private MockSvrSideJdkChannel mockChannel = new MockSvrSideJdkChannel(); 
	private MockSvrChannel mockSvrChannel = new MockSvrChannel();
	private MockJdk mockJdk = new MockJdk(mockSvrChannel);
	private MockAsyncListener listener;

	@Before
	public void setup() throws InterruptedException, ExecutionException, TimeoutException {
		MeterRegistry meters = Metrics.globalRegistry;

		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk, meters);
		DirectExecutor exec = new DirectExecutor();
		BackpressureConfig config = new BackpressureConfig();
		config.setMaxBytes(6);
		config.setStartReadingThreshold(2);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("test'n", new BufferCreationPool(), config, exec);

		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(mgr, meters);
		
		listener = new MockAsyncListener();

		AsyncServer server = svrMgr.createTcpServer(new AsyncConfig(), listener);
		CompletableFuture<Void> future = server.start(new InetSocketAddress(4444));
		Assert.assertFalse(future.isDone());

		mockJdk.setThread(Thread.currentThread());
		mockJdk.fireSelector();
		
		future.get(2, TimeUnit.SECONDS);
		
		mockSvrChannel.addNewChannel(mockChannel);
		mockJdk.setThread(Thread.currentThread());
		mockJdk.fireSelector();

		Assert.assertEquals(1, listener.getNumTimesCalledConnectionOpen());
	}

	@Test
	public void testBasicConnectingOnSelectorThread() {

		mockChannel.forceDataRead(mockJdk, new byte[] { 4, 5 });

		byte[] data = listener.getSingleData();
		Assert.assertEquals(data[0], 4);
		Assert.assertEquals(data[1], 5);
	}

	@Test
	public void testBackpressureRead() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<Void> future1 = new CompletableFuture<Void>();
		CompletableFuture<Void> future2 = new CompletableFuture<Void>();
		CompletableFuture<Void> future3 = new CompletableFuture<Void>();
		CompletableFuture<Void> future4 = new CompletableFuture<Void>();

		listener.addIncomingRetValue(future1);
		listener.addIncomingRetValue(future2);
		listener.addIncomingRetValue(future3);
		listener.addIncomingRetValue(future4);
		
		mockChannel.forceDataRead(mockJdk, new byte[] { 1, 2, 3 });

		Assert.assertEquals(3, listener.getSingleData().length);
		
		mockChannel.forceDataRead(mockJdk, new byte[] { 4, 5 });

		Assert.assertEquals(2, listener.getSingleData().length);

		mockChannel.forceDataRead(mockJdk, new byte[] { 6, 7, 8 });
		
		Assert.assertEquals(3, listener.getSingleData().length);

		mockChannel.forceDataRead(mockJdk, new byte[] { 6, 7, 8, 9 });
		
		Assert.assertEquals(0, listener.getNumTimesCalledIncomingData());
		
		future2.complete(null); //ack reading the 2nd payload
		mockJdk.fireSelector();		
		Assert.assertEquals(0, listener.getNumTimesCalledIncomingData());

		future1.complete(null);
		mockJdk.fireSelector();		
		Assert.assertEquals(0, listener.getNumTimesCalledIncomingData());

		future3.complete(null);
		mockJdk.fireSelector();		
		
		Assert.assertEquals(4, listener.getSingleData().length);
	}
	
}
