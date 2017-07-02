package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.mocks.MockClientSideJdkChannel;
import org.webpieces.nio.api.mocks.MockDataListener;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.util.threading.DirectExecutor;

public class TestReading {

	private ChannelManager mgr;
	private MockClientSideJdkChannel mockChannel = new MockClientSideJdkChannel();
	private MockJdk mockJdk = new MockJdk(mockChannel);
	private TCPChannel channel;
	private MockDataListener listener;

	@Before
	public void setup() throws InterruptedException, ExecutionException, TimeoutException {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk);
		DirectExecutor exec = new DirectExecutor();
		BackpressureConfig config = new BackpressureConfig();
		config.setMaxBytes(6);
		config.setStartReadingThreshold(2);
		mgr = factory.createMultiThreadedChanMgr("test'n", new BufferCreationPool(), config, exec);
		
		listener = new MockDataListener();
		
		channel = mgr.createTCPChannel("myid");
		
		mockChannel.addConnectReturnValue(true);
		mockJdk.setThread(Thread.currentThread());
		CompletableFuture<Void> future = channel.connect(new InetSocketAddress(4444), listener);
		future.get(2, TimeUnit.SECONDS);
		Assert.assertTrue(mockChannel.isRegisteredForReads());
	}

	@Test
	public void testRead() throws InterruptedException, ExecutionException, TimeoutException {
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
