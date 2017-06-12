package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.mocks.MockAsyncListener;
import org.webpieces.nio.api.mocks.MockChannel;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.nio.api.mocks.MockSvrChannel;
import org.webpieces.util.threading.DirectExecutor;

public class TestAccepting {

	private MockChannel mockChannel = new MockChannel(); 
	private MockSvrChannel mockSvrChannel = new MockSvrChannel();
	private MockJdk mockJdk = new MockJdk(mockSvrChannel);
	private AsyncServerManager svrMgr;

	@Before
	public void setup() {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk);
		DirectExecutor exec = new DirectExecutor();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("test'n", new BufferCreationPool(), new BackpressureConfig(), exec);

		svrMgr = AsyncServerMgrFactory.createAsyncServer(mgr);
	}

	@Test
	public void testBasicConnectingOnSelectorThread() {
		MockAsyncListener listener = new MockAsyncListener();

		AsyncServer server = svrMgr.createTcpServer(new AsyncConfig(), listener);
		CompletableFuture<Void> future = server.start(new InetSocketAddress(4444));
		Assert.assertFalse(future.isDone());

		mockSvrChannel.addNewChannel(mockChannel);
		mockJdk.setThread(Thread.currentThread());
		mockJdk.fireSelector();

		Assert.assertEquals(1, listener.getNumTimesCalledConnectionOpen());

	}

}
