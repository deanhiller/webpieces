package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient.api.mocks.MockChannel;
import org.webpieces.httpclient.api.mocks.MockChannelMgr;
import org.webpieces.httpclient.api.mocks.MockResponseListener;
import org.webpieces.httpclientx.api.Http2to1_1ClientFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestWriteReads {

	private MockChannelMgr mockChannelMgr = new MockChannelMgr();
	private MockChannel mockChannel = new MockChannel();
	private Http2Client httpClient;
	private Http2Socket socket;

	@Before
	public void setup() throws InterruptedException, ExecutionException, TimeoutException {
		BufferPool pool = new BufferCreationPool();
		httpClient = Http2to1_1ClientFactory.createHttpClient("myClient4", mockChannelMgr, new SimpleMeterRegistry(), pool);
		
		mockChannelMgr.addTCPChannelToReturn(mockChannel);
		socket = httpClient.createHttpSocket();

		mockChannel.setConnectFuture(CompletableFuture.completedFuture(null));
		CompletableFuture<Void> future = socket.connect(new InetSocketAddress(8080));
		
		future.get(2, TimeUnit.SECONDS);
	}

	@Test
	public void testBasicReadWrite() throws InterruptedException, ExecutionException, TimeoutException {
		MockResponseListener listener = new MockResponseListener();
		StreamHandle handle = socket.openStream();

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		Http2Request request = Requests.createRequest();
		CompletableFuture<StreamWriter> writer = handle.process(request, listener);
		Assert.assertTrue(writer.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		MockResponseListener listener2 = new MockResponseListener();
		request.getHeaderLookupStruct().getHeader("serverid").setValue("2");
		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		CompletableFuture<StreamWriter> writer2 = handle.process(request, listener2);
		Assert.assertTrue(writer2.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		Http2Response response1 = Requests.createResponse(1, 0);
		listener.addProcessResponse(CompletableFuture.completedFuture(null));
		CompletableFuture<Void> fut1 = mockChannel.writeResponse(response1);
		fut1.get(2, TimeUnit.SECONDS); //throws if exception occurred and ensures future resolved
		
		Http2Response msg = listener.getIncomingMsg();
		Assert.assertEquals(response1, msg);
		
		Http2Response response2 = Requests.createResponse(2, 0);
		listener2.addProcessResponse(CompletableFuture.completedFuture(null));
		CompletableFuture<Void> fut2 = mockChannel.writeResponse(response2);
		fut2.get(2, TimeUnit.SECONDS); //throws if exception occurred and ensures future resolved
		Http2Response msg2 = listener2.getIncomingMsg();
		Assert.assertEquals(response2, msg2);
	}

	
}
