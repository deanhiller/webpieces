package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.TwoPools;
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
	private HttpClient httpClient;
	private HttpSocket httpSocket;
	
	@Before
	public void setup() {
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		TwoPools pool = new TwoPools("client.bufferpool", metrics);
		HttpParser parser = HttpParserFactory.createParser("testParser", metrics, pool);
		httpClient = HttpClientFactory.createHttpClient("testClient", mockChanMgr, parser);
		
		httpSocket = httpClient.createHttpSocket();

	}

	//TODO(dhiller): Write test
	@Test
	public void testRequestResponseCompletableFutureCancelNoKeepAlive() {
		
	}

	//TODO(dhiller): Write test
	@Test
	public void testRequestResponseCompletableFutureCancelWithKeepAlive() {
		
	}

	//TODO(dhiller): Write test
	@Test
	public void testClientCancelWithKeepAlive() {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(85555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
		req.addHeader(new Header(KnownHeaderName.CONNECTION, "keep-alive"));
		HttpStreamRef ref = httpSocket.send(req, mockListener);
		
		ref.cancel("some reason");
		
		//Assert the socket is NOT closed
		
	}

	//TODO(dhiller): Write test
	@Test
	public void testClientCancelNoKeepAlive() {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(85555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
		HttpStreamRef ref = httpSocket.send(req, mockListener);
		
		ref.cancel("some reason");
		
		//Assert the socket is closed
	}

	//TODO(dhiller): Write test
	@Test
	public void testServerCloseSocket() {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(85555));
		MockResponseListener mockListener = new MockResponseListener();
		
		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
		httpSocket.send(req, mockListener);
		
		
		
	}
}
