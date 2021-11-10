package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import org.webpieces.util.futures.XFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.nio.api.mocks.MockAsyncListener;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.nio.api.mocks.MockSvrChannel;
import org.webpieces.nio.api.mocks.MockSvrSideJdkChannel;
import org.webpieces.util.threading.DirectExecutor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestAccepting {

	private MockSvrSideJdkChannel mockChannel = new MockSvrSideJdkChannel(); 
	private MockSvrChannel mockSvrChannel = new MockSvrChannel();
	private MockJdk mockJdk = new MockJdk(mockSvrChannel);
	private AsyncServerManager svrMgr;

	@Before
	public void setup() {
		MeterRegistry meters = Metrics.globalRegistry;
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk, meters);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("test'n", new TwoPools("pl", new SimpleMeterRegistry()), new BackpressureConfig(), new DirectExecutor());

		svrMgr = AsyncServerMgrFactory.createAsyncServer(mgr, meters);
	}

	@Test
	public void testBasicConnectingOnSelectorThread() {
		MockAsyncListener listener = new MockAsyncListener();

		AsyncServer server = svrMgr.createTcpServer(new AsyncConfig(), listener);
		XFuture<Void> future = server.start(new InetSocketAddress(4444));
		Assert.assertFalse(future.isDone());

		mockSvrChannel.addNewChannel(mockChannel);
		mockJdk.setThread(Thread.currentThread());
		mockJdk.fireSelector();

		Assert.assertEquals(1, listener.getNumTimesCalledConnectionOpen());

	}

}
