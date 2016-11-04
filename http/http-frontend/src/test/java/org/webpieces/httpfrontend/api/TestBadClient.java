package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class TestBadClient {

	private MockTcpChannel mockServerChannel = new MockTcpChannel();
	private MockTcpServerChannel mockChannel = new MockTcpServerChannel();
	private MockChannelManager mockChanMgr = new MockChannelManager();
	private MockTimer timer = new MockTimer();
	private RequestListenerForTest requestListenerForTest = new RequestListenerForTest();

	private HttpFrontendManager mgr;
	
	@Before
	public void setup() {
		mockChanMgr.addTcpSvrChannel(mockChannel);
		AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(mockChanMgr);
		BufferCreationPool pool = new BufferCreationPool();
		mgr = HttpFrontendFactory.createFrontEnd(svrManager, timer, pool);
	}
	
	@Test
	public void testTimeoutTimerCancelled() throws InterruptedException, ExecutionException {
		FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(80));
		config.maxConnectToRequestTimeoutMs = 5000;
		mgr.createHttpServer(config , requestListenerForTest);
		
		ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
		Assert.assertEquals(1, listeners.length);
		
		MockFuture<?> mockFuture = new MockFuture<>();
		timer.addMockFuture(mockFuture);
		ConnectionListener listener = listeners[0];
		CompletableFuture<DataListener> future = listener.connected(mockServerChannel, true);

		//expect timer task to be scheduled...
		ParametersPassedIn[] methodCalls = timer.getScheduledTimers();
		Assert.assertEquals("Expecting that timer.schedule is called once", 1, methodCalls.length);

		DataListener dataListener = future.get();
		ByteBuffer buffer = ByteBuffer.wrap("\r\n\r\nasdfsdf".getBytes());
		dataListener.incomingData(mockServerChannel, buffer);
		
		//verify our connection was not closed
		Assert.assertTrue(mockServerChannel.isClosed());
		//verify our timeout was cancelled..
		Assert.assertTrue(mockFuture.isCancelled());
	}

	@Test
	public void testMaxSizeExceeded() throws InterruptedException, ExecutionException {
		FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(80));
		config.maxConnectToRequestTimeoutMs = 5000;
		mgr.createHttpServer(config , requestListenerForTest);
		
		ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
		Assert.assertEquals(1, listeners.length);
		
		MockFuture<?> mockFuture = new MockFuture<>();
		timer.addMockFuture(mockFuture);
		ConnectionListener listener = listeners[0];
		CompletableFuture<DataListener> future = listener.connected(mockServerChannel, true);

		//expect timer task to be scheduled...
		ParametersPassedIn[] methodCalls = timer.getScheduledTimers();
		Assert.assertEquals("Expecting that timer.schedule is called once", 1, methodCalls.length);

		DataListener dataListener = future.get();
		ByteBuffer buffer = ByteBuffer.wrap("\r\n\r\nasdfsdf".getBytes());
		dataListener.incomingData(mockServerChannel, buffer);
		
		//verify our connection was not closed
		Assert.assertTrue(mockServerChannel.isClosed());
		//verify our timeout was cancelled..
		Assert.assertTrue(mockFuture.isCancelled());
	}
}
