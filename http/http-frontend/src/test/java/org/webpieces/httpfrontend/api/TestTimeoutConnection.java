package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.handlers.ConnectionListener;

public class TestTimeoutConnection {

	private static final Logger log = LoggerFactory.getLogger(TestTimeoutConnection.class);

	@Test
	public void testNoWriteDataTimeout() throws InterruptedException, ExecutionException, TimeoutException {
		MockTcpChannel mockServerChannel = new MockTcpChannel();
		MockTcpServerChannel mockChannel = new MockTcpServerChannel();
		MockChannelManager mockChanMgr = new MockChannelManager();
		mockChanMgr.addTcpSvrChannel(mockChannel);
		AsyncServerManager svrManager = AsyncServerMgrFactory.createAsyncServer(mockChanMgr);
		BufferCreationPool pool = new BufferCreationPool();
		HttpParser parser = HttpParserFactory.createParser(pool);
		HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd(svrManager, parser);
		
		MockRequestListener mockRequestListener = new MockRequestListener();
		AsyncConfig config = new AsyncConfig("asdf", new InetSocketAddress(0));
		HttpFrontend httpSvr = mgr.createHttpServer(config , mockRequestListener );
		
		ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
		Assert.assertEquals(1, listeners.length);
		
		ConnectionListener listener = listeners[0];
		listener.connected(mockServerChannel);
		
		//send no data and wait for timeout..
		
		
//		AsyncConfig config = new AsyncConfig("tcpServer", new InetSocketAddress(8080));
//		BufferCreationPool pool = new BufferCreationPool();
//		AsyncServerManager server = AsyncServerMgrFactory.createAsyncServer("server", pool);
//		server.createTcpServer(config, new IntegTestClientNotReadListener());
//		
//		BufferCreationPool pool2 = new BufferCreationPool();
//		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
//		ChannelManager mgr = factory.createSingleThreadedChanMgr("client", pool2);
//		TCPChannel channel = mgr.createTCPChannel("clientChan");
//
//		log.info("client");
//
//		MockDataListener listener = new MockDataListener();
//		CompletableFuture<Channel> connect = channel.connect(new InetSocketAddress(8080), listener);
//		//wait for connection
//		connect.get(10, TimeUnit.SECONDS);
//
//		//Find something to mock and fire the timer from inside AsyncSvrManager so we can change this
//		//to sleep(1000) ...sleep is required since we are doing this over teh socket :( and it involves nic buffers
//		//BUT we really want to make sure someone doesn't break this feature
//		Thread.sleep(5000);
//		
//		//The server should have timed out since we did not send any data in...
//		Assert.assertTrue(listener.isClosed());
	}

}
