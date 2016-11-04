package org.webpieces.httpfrontend.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class TestTimeoutConnection {

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
	
	/**
	 * This tests by mocking channelmanager out such that we have full control INCLUDING the ability to throw
	 * any exceptions we want...
	 */
	@Test
	public void testNoWriteDataTimeout() throws InterruptedException, ExecutionException, TimeoutException {
		long timeout = 6000;
		
		FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(80));
		config.maxConnectToRequestTimeoutMs = (int) timeout;

		mgr.createHttpServer(config , requestListenerForTest);
		
		ConnectionListener[] listeners = mockChanMgr.fetchTcpConnectionListeners();
		Assert.assertEquals(1, listeners.length);
		
		MockFuture<?> mockFuture = new MockFuture<>();
		timer.addMockFuture(mockFuture);
		ConnectionListener listener = listeners[0];
		listener.connected(mockServerChannel, true);

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
		//verify our timeout was not cancelled..
		Assert.assertTrue(!mockFuture.isCancelled());
	}
	
	@Test
	public void testTimeoutTimerCancelled() throws InterruptedException, ExecutionException {
		long timeout = 6000;
		
		FrontendConfig config = new FrontendConfig("httpFrontend", new InetSocketAddress(80));
		config.maxConnectToRequestTimeoutMs = (int) timeout;

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
		ByteBuffer buffer = createHttpRequest();
		dataListener.incomingData(mockServerChannel, buffer);
		
		//verify our connection was not closed
		Assert.assertTrue(!mockServerChannel.isClosed());
		//verify our timeout was cancelled..
		Assert.assertTrue(mockFuture.isCancelled());
	}

	private ByteBuffer createHttpRequest() {
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.POST);
		requestLine.setUri(new HttpUri("http://myhost.com"));
		
		HttpRequest request = new HttpRequest();
		request.setRequestLine(requestLine);
		
		HttpParser parser = HttpParserFactory.createParser(new BufferCreationPool());
		
		byte[] bytes = parser.marshalToBytes(request);
		return ByteBuffer.wrap(bytes);
	}
}
