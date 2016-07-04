package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;

public class TestTimeoutConnection {

	private static final Logger log = LoggerFactory.getLogger(TestTimeoutConnection.class);

	/**
	 * This tests by mocking channelmanager out such that we have full control INCLUDING the ability to throw
	 * any exceptions we want...
	 */
	@Test
	public void testNoWriteDataTimeout() throws InterruptedException, ExecutionException, TimeoutException {
		long timeout = 6000;
		MockTcpChannel mockServerChannel = new MockTcpChannel();
		MockTcpServerChannel mockChannel = new MockTcpServerChannel();
		MockChannelManager mockChanMgr = new MockChannelManager();
		MockTimer timer = new MockTimer();
		mockChanMgr.addTcpSvrChannel(mockChannel);
		AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(mockChanMgr);
		BufferCreationPool pool = new BufferCreationPool();
		HttpParser parser = HttpParserFactory.createParser(pool);
		HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd(svrManager, timer, parser);
		
		MockRequestListener mockRequestListener = new MockRequestListener();
		//AsyncConfig config = new AsyncConfig("asdf", new InetSocketAddress(0));
		FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(80));
		config.maxConnectToRequestTimeoutMs = (int) timeout;

		HttpFrontend httpSvr = mgr.createHttpServer(config , mockRequestListener );
		
		ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
		Assert.assertEquals(1, listeners.length);
		
		ConnectionListener listener = listeners[0];
		listener.connected(mockServerChannel);

		//expect timer task to be scheduled...
		ParametersPassedIn[] methodCalls = timer.getScheduledTimers();
		Assert.assertEquals("Expecting that timer.schedule is called once", 1, methodCalls.length);
		ParametersPassedIn call = methodCalls[0];
		Runnable timerTask = (Runnable) call.getArgs()[0];
		long time = (Long)call.getArgs()[1];
		Assert.assertEquals(timeout, time);
		
		//now, simulate the timeout
		timerTask.run();
		
		//verify our connection was closed
		Assert.assertTrue(mockServerChannel.isClosed());
		
	}
	/**
	 * This is a more full test and more realistic of the system
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 */
	@Test
	public void testIntegrationOfTimeoutAfterConnection() throws InterruptedException, ExecutionException, TimeoutException {
		ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(2);
		HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10, timer);
		
		FrontendConfig config = new FrontendConfig("tcpServer", new InetSocketAddress(0));
		config.maxConnectToRequestTimeoutMs = 1000;
		MockRequestListener mockRequestListener = new MockRequestListener();
		HttpFrontend server = mgr.createHttpServer(config, mockRequestListener);
		
		int port = server.getUnderlyingChannel().getLocalAddress().getPort();
		
		BufferCreationPool pool2 = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createSingleThreadedChanMgr("client", pool2);
		TCPChannel channel = chanMgr.createTCPChannel("clientChan");
		
		MockDataListener listener = new MockDataListener();
		CompletableFuture<Channel> connect = channel.connect(new InetSocketAddress(port), listener);

		//wait for connection
		connect.get(10, TimeUnit.SECONDS);

		//Find something to mock and fire the timer from inside AsyncSvrManager so we can change this
		//to sleep(1000) ...sleep is required since we are doing this over teh socket :( and it involves nic buffers
		//BUT we really want to make sure someone doesn't break this feature
		Thread.sleep(2000);
		
		Assert.assertTrue(listener.isClosed());
		Assert.assertTrue(mockRequestListener.isClosed());
	}

}
