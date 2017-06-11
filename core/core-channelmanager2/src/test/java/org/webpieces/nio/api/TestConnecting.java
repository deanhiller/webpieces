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
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.mocks.MockChannel;
import org.webpieces.nio.api.mocks.MockDataListener;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.util.threading.DirectExecutor;

public class TestConnecting {

	private ChannelManager mgr;
	private MockChannel mockChannel = new MockChannel();
	private MockJdk mockJdk = new MockJdk(mockChannel);

	@Before
	public void setup() {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk);
		DirectExecutor exec = new DirectExecutor();
		mgr = factory.createMultiThreadedChanMgr("test'n", new BufferCreationPool(), new BackpressureConfig(), exec);
	}

	@Test
	public void testBasicConnectingOnSelectorThread() {
		MockDataListener listener = new MockDataListener();
		
		TCPChannel channel = mgr.createTCPChannel("myid");
		
		mockChannel.addConnectReturnValue(true);
		mockJdk.setThread(Thread.currentThread());
		CompletableFuture<Channel> future = channel.connect(new InetSocketAddress(4444), listener);
		Assert.assertTrue(future.isDone());
		Assert.assertTrue(mockChannel.isRegisteredForReads());
	}
	
	@Test
	public void testBasicConnectingNotOnSelectorThreadAtFirst() {
		MockDataListener listener = new MockDataListener();
		
		TCPChannel channel = mgr.createTCPChannel("myid");
		
		mockChannel.addConnectReturnValue(true);
		CompletableFuture<Channel> future = channel.connect(new InetSocketAddress(4444), listener);
		Assert.assertFalse(future.isDone());
		Assert.assertFalse(mockChannel.isRegisteredForReads());
		
		mockJdk.setThread(Thread.currentThread()); //simulate being on selector thread
		mockJdk.fireSelector();		
		Assert.assertTrue(future.isDone());
		Assert.assertTrue(mockChannel.isRegisteredForReads());		
	}

	@Test
	public void testConnectingDelayedAndOnSelectorThread() throws InterruptedException, ExecutionException, TimeoutException {
		MockDataListener listener = new MockDataListener();
		
		TCPChannel channel = mgr.createTCPChannel("myid");
		
		mockChannel.addConnectReturnValue(false);
		mockJdk.setThread(Thread.currentThread());
		CompletableFuture<Channel> future = channel.connect(new InetSocketAddress(4444), listener);
		Assert.assertFalse(future.isDone());
		
		Assert.assertFalse(mockChannel.isRegisteredForReads());
		Assert.assertEquals(0, mockChannel.getNumTimesFinishConnectCalled());

		//NOW, fire the selector AND return ready for finishing connection so the connection future resolves
		mockChannel.setReadyToConnect();
		mockJdk.fireSelector();

		Assert.assertTrue(mockChannel.isRegisteredForReads());
		Assert.assertEquals(1, mockChannel.getNumTimesFinishConnectCalled());
		future.get(2, TimeUnit.SECONDS);
	}
	
	@Test
	public void testConnectingDelayedAndNotOnSelectorThread() throws InterruptedException, ExecutionException, TimeoutException {
		MockDataListener listener = new MockDataListener();
		
		TCPChannel channel = mgr.createTCPChannel("myid");
		
		mockChannel.addConnectReturnValue(false);
		CompletableFuture<Channel> future = channel.connect(new InetSocketAddress(4444), listener);
		
		Assert.assertEquals(1, mockJdk.getNumTimesWokenUp());
		Assert.assertFalse(future.isDone());
		
		mockJdk.setThread(Thread.currentThread()); //simulate being on selector thread
		//next simulate the selector waking up and firing
		mockJdk.fireSelector();

		Assert.assertFalse(future.isDone());//still not done
		Assert.assertEquals(0, mockChannel.getNumTimesFinishConnectCalled());
		Assert.assertFalse(mockChannel.isRegisteredForReads());

		//NOW, fire the selector AND return ready for finishing connection so the connection future resolves
		mockChannel.setReadyToConnect();
		mockJdk.fireSelector();

		Assert.assertTrue(mockChannel.isRegisteredForReads());
		Assert.assertEquals(1, mockChannel.getNumTimesFinishConnectCalled());
		future.get(2, TimeUnit.SECONDS);
		
	}

	@Test
	public void testWriteBeforeConnection() throws InterruptedException, ExecutionException, TimeoutException {
		MockDataListener listener = new MockDataListener();
		
		TCPChannel channel = mgr.createTCPChannel("myid");
		
		mockChannel.addConnectReturnValue(false);
		CompletableFuture<Channel> future = channel.connect(new InetSocketAddress(4444), listener);
		
		Assert.assertEquals(1, mockJdk.getNumTimesWokenUp());
		Assert.assertFalse(future.isDone());
		
		mockJdk.setThread(Thread.currentThread()); //simulate being on selector thread
		//next simulate the selector waking up and firing
		mockJdk.fireSelector();

		Assert.assertFalse(future.isDone());//still not done
		Assert.assertEquals(0, mockChannel.getNumTimesFinishConnectCalled());
		Assert.assertFalse(mockChannel.isRegisteredForReads());

		try {
			channel.write(ByteBuffer.wrap(new byte[] { 1 }));
			Assert.fail("should have thrown exception since channel is not connected yet");
		} catch(NioException e) {
		}
		
		//NOW, fire the selector AND return ready for finishing connection so the connection future resolves
		mockChannel.setReadyToConnect();
		mockJdk.fireSelector();
		
		Assert.assertTrue(mockChannel.isRegisteredForReads());
		Assert.assertEquals(1, mockChannel.getNumTimesFinishConnectCalled());
		future.get(2, TimeUnit.SECONDS);

		channel.write(ByteBuffer.wrap(new byte[] { 1 }));
	}
}
