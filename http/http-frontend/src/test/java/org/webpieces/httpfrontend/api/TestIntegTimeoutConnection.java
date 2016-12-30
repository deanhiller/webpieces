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
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

public class TestIntegTimeoutConnection {
	
	/**
	 * This is a more full test and more realistic of the system
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 */
	@Test
	public void testIntegrationOfTimeoutAfterConnection() throws InterruptedException, ExecutionException, TimeoutException {
		ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(2);
		BufferCreationPool pool = new BufferCreationPool();
		HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10, timer, pool);
		
		FrontendConfig config = new FrontendConfig("tcpServer", new InetSocketAddress(0));
		config.maxConnectToRequestTimeoutMs = 1000;
		RequestListenerForTest requestListenerForTest = new RequestListenerForTest();
		HttpServer server = mgr.createHttpServer(config, requestListenerForTest);
		
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
		//to sleep(1000) ...sleep is required since we are doing this over the socket :( and it involves nic buffers
		//BUT we really want to make sure someone doesn't break this feature
		Thread.sleep(2000);
		
		Assert.assertTrue(listener.isClosed());
		Assert.assertTrue(requestListenerForTest.isClosed());
	}

	@Test
	public void testIntegrationOfTimeoutAfterSslConnection() throws InterruptedException, ExecutionException, TimeoutException {
		ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(2);
		BufferCreationPool pool = new BufferCreationPool();
		HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd("frontEnd", 10, timer, pool);
		
		FrontendConfig config = new FrontendConfig("tcpServer", new InetSocketAddress(0));
		config.maxConnectToRequestTimeoutMs = 1000;
		RequestListenerForTest requestListenerForTest = new RequestListenerForTest();
		HttpServer server = mgr.createHttpsServer(config, requestListenerForTest, new SSLEngineFactoryForTest());
		
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
		Assert.assertTrue(requestListenerForTest.isClosed());
	}
}
