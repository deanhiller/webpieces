package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
import org.webpieces.httpclient11.api.SocketClosedException;
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

		mockChannel.setConnectFuture(CompletableFuture.completedFuture(null));

		mockChanMgr.addTCPChannelToReturn(mockChannel);
		httpSocket = httpClient.createHttpSocket();
	}

//	@Test
//	public void testRequestResponseCompletableFutureCancelNoKeepAlive() {
//		throw new UnsupportedOperationException("not done yet");
//	}
//
//	@Test
//	public void testRequestResponseCompletableFutureCancelWithKeepAlive() {
//		throw new UnsupportedOperationException("not done yet");
//	}

	@Test
	public void testClientCancelWithKeepAlive() {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
		req.addHeader(new Header(KnownHeaderName.CONNECTION, "keep-alive"));

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		HttpStreamRef ref = httpSocket.send(req, mockListener);
		
		CompletableFuture<Void> cancelDone = ref.cancel("some reason");
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertFalse(mockChannel.isClosed());
	}

	@Test
	public void testClientCancelNoKeepAlive() {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		HttpStreamRef ref = httpSocket.send(req, mockListener);
		
		CompletableFuture<Void> cancelDone = ref.cancel("some reason");
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertTrue(mockChannel.isClosed());
	}

	@Test
	public void testServerCloseSocket() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		httpSocket.send(req, mockListener);

		mockChannel.simulateClose();
		
		Assert.assertTrue(mockListener.isClosed());
		
	}
}