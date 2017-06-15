package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.mocks.MockChannel;
import org.webpieces.nio.api.mocks.MockDataListener;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.util.threading.DirectExecutor;

public class TestWriting {

	private ChannelManager mgr;
	private MockChannel mockChannel = new MockChannel();
	private MockJdk mockJdk = new MockJdk(mockChannel);
	private TCPChannel channel;

	@Before
	public void setup() throws InterruptedException, ExecutionException, TimeoutException {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk);
		DirectExecutor exec = new DirectExecutor();
		mgr = factory.createMultiThreadedChanMgr("test'n", new BufferCreationPool(), new BackpressureConfig(), exec);
		
		MockDataListener listener = new MockDataListener();
		
		channel = mgr.createTCPChannel("myid");
		
		mockChannel.addConnectReturnValue(true);
		mockJdk.setThread(Thread.currentThread());
		CompletableFuture<Void> future = channel.connect(new InetSocketAddress(4444), listener);
		future.get(2, TimeUnit.SECONDS);
		Assert.assertTrue(mockChannel.isRegisteredForReads());
	}

	@Test
	public void testImmediateWrite() throws InterruptedException, ExecutionException, TimeoutException {
		mockChannel.setNumBytesToConsume(2);
		CompletableFuture<Void> future = channel.write(ByteBuffer.wrap(new byte[] { 1, 3 }));
		future.get(2, TimeUnit.SECONDS);
		
		Assert.assertEquals(1, mockChannel.nextByte());
		Assert.assertEquals(3, mockChannel.nextByte());
	}
	
	@Test
	public void testDelayedWrite() throws InterruptedException, ExecutionException, TimeoutException {
		
		CompletableFuture<Void> future = channel.write(ByteBuffer.wrap(new byte[] { 2, 5 }));
		Assert.assertFalse(future.isDone());		
		Assert.assertTrue(mockChannel.isRegisteredForWrites());
		
		mockChannel.setReadyToWrite();
		mockChannel.setNumBytesToConsume(2);
		mockJdk.setThread(Thread.currentThread()); //simulate being on selector thread
		//next simulate the selector waking up and firing
		mockJdk.fireSelector();
		
		Assert.assertEquals(2, mockChannel.nextByte());
		Assert.assertEquals(5, mockChannel.nextByte());
	}

	@Test
	public void testDelayedSplitWrite() throws InterruptedException, ExecutionException, TimeoutException {
		mockJdk.setThread(null); //simulate write not on selector thread

		mockChannel.setNumBytesToConsume(1);

		CompletableFuture<Void> future = channel.write(ByteBuffer.wrap(new byte[] { 9, 5 }));
		Assert.assertFalse(future.isDone());
		Assert.assertEquals(1, mockChannel.getNumBytesConsumed());
		Assert.assertEquals(9, mockChannel.nextByte());
		
		mockChannel.setNumBytesToConsume(1);
		mockChannel.setReadyToWrite();
		mockJdk.setThread(Thread.currentThread()); //simulate being on selector thread
		//next simulate the selector waking up and firing
		mockJdk.fireSelector();

		Assert.assertFalse(mockChannel.isRegisteredForWrites());
		Assert.assertEquals(5, mockChannel.nextByte());
	}
}
