package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpclient.mocks.MockChannel;
import org.webpieces.httpclient.mocks.MockChannelMgr;
import org.webpieces.httpclient.mocks.MockResponseListener;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpClientFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpStreamRef;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestChunking {

	private static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
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

	@Test
	public void testChunkingHeaderMissing() throws InterruptedException, ExecutionException, TimeoutException {
//		XFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
//		MockResponseListener mockListener = new MockResponseListener();
//		
//		HttpRequest req = Requests.createRequest(KnownHttpMethod.GET, "/home", false);
//		mockChannel.addWriteResponse(XFuture.completedFuture(null));
//		HttpStreamRef ref = httpSocket.send(req, mockListener);
//
//		HttpDataWriter writer = ref.getWriter().get(2, TimeUnit.SECONDS);
//		
//		HttpChunk c = new HttpChunk(dataGen.emptyWrapper());
//		XFuture<Void> result = writer.send(c);
//		
//		try {
//			result.get(2, TimeUnit.SECONDS);
//			Assert.fail("Should have thrown exception.  can't allow sending data when missing transfer encoding header");
//		} catch(ExecutionException e) {
//			IllegalStateException exc = (IllegalStateException) e.getCause();
//			Assert.assertTrue(exc.getMessage().contains("Header Transfer-Encoding was not set with"));
//		}
	}

}