package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.mocks.MockChannel;
import org.webpieces.nio.api.mocks.MockDataListener;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.util.threading.DirectExecutor;

public class TestReading {

	private ChannelManager mgr;
	private MockChannel mockChannel = new MockChannel();
	private MockJdk mockJdk = new MockJdk(mockChannel);
	private TCPChannel channel;
	private MockDataListener listener;

	@Before
	public void setup() {
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
		CompletableFuture<Channel> future = channel.connect(new InetSocketAddress(4444), listener);
		Assert.assertTrue(future.isDone());
		Assert.assertTrue(mockChannel.isRegisteredForReads());
	}

	@Test
	public void testRead() throws InterruptedException, ExecutionException, TimeoutException {
		mockChannel.addToRead(new byte[] { 4, 5 });
		mockChannel.setReadyToRead();
		
		mockJdk.fireSelector();

		byte[] data = listener.getSingleData();
		Assert.assertEquals(data[0], 4);
		Assert.assertEquals(data[1], 5);
	}

	@Test
	public void testBackpressureRead() throws InterruptedException, ExecutionException, TimeoutException {
//		mockChannel.addToRead(new byte[] { 4, 5 });
//		mockChannel.setReadyToRead();
//		
//		mockJdk.fireSelector();
//
//		byte[] data = listener.getSingleData();
//		Assert.assertEquals(data[0], 4);
//		Assert.assertEquals(data[1], 5);
	}
}
