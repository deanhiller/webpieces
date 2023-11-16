package org.webpieces.httpclient;

import org.webpieces.util.HostWithPort;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpclient.mocks.MockChannel;
import org.webpieces.httpclient.mocks.MockChannelMgr;
import org.webpieces.httpclient.mocks.MockResponseListener;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpClientFactory;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpStreamRef;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestCancelStream {

	private MockChannelMgr mockChanMgr = new MockChannelMgr();
	private MockChannel mockChannel = new MockChannel();
	private HttpClient httpClient;
	private HttpSocket httpSocket;
	
	@Before
	public void setup() {
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		TwoPools pool = new TwoPools("client.bufferpool", metrics);
		HttpParser parser = HttpParserFactory.createParser("testParser", metrics, pool);
		httpClient = HttpClientFactory.createHttpClient("testClient", mockChanMgr, parser);

		mockChannel.setConnectFuture(XFuture.completedFuture(null));

		mockChanMgr.addTCPChannelToReturn(mockChannel);
		httpSocket = httpClient.createHttpSocket(new SocketListener());
	}

//	@Test
//	public void testRequestResponseXFutureCancelNoKeepAlive() {
//		throw new UnsupportedOperationException("not done yet");
//	}
//
//	@Test
//	public void testRequestResponseXFutureCancelWithKeepAlive() {
//		throw new UnsupportedOperationException("not done yet");
//	}

	@Test
	public void testClientCancelWithKeepAlive() {
		XFuture<Void> connect = httpSocket.connect(new HostWithPort(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
		req.addHeader(new Header(KnownHeaderName.CONNECTION, "keep-alive"));

		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		HttpStreamRef ref = httpSocket.send(req, mockListener);
		
		XFuture<Void> cancelDone = ref.cancel("some reason");
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertFalse(mockChannel.isClosed());
	}

	@Test
	public void testClientCancelNoKeepAlive() {
		XFuture<Void> connect = httpSocket.connect(new HostWithPort(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		HttpStreamRef ref = httpSocket.send(req, mockListener);
		
		XFuture<Void> cancelDone = ref.cancel("some reason");
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertTrue(mockChannel.isClosed());
	}

	@Test
	public void testServerCloseSocket() throws InterruptedException, ExecutionException, TimeoutException {
		XFuture<Void> connect = httpSocket.connect(new HostWithPort(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);

		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		httpSocket.send(req, mockListener);

		mockChannel.simulateClose();
		
		Assert.assertTrue(mockListener.isClosed());
		
	}
}