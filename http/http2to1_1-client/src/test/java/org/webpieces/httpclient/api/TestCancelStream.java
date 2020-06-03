package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.TwoPools;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient.api.mocks.MockChannel;
import org.webpieces.httpclient.api.mocks.MockChannelMgr;
import org.webpieces.httpclient.api.mocks.MockResponseListener;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestCancelStream {

	private MockChannelMgr mockChanMgr = new MockChannelMgr();
	private MockChannel mockChannel = new MockChannel();
	private Http2Client httpClient;
	private Http2Socket httpSocket;
	
	@Before
	public void setup() {
		SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		TwoPools pool = new TwoPools("client.bufferpool", metrics);
		httpClient = Http2to11ClientFactory.createHttpClient("myClient2", mockChanMgr, new SimpleMeterRegistry(), pool);

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
		
		Http2Request req = Requests.createRequest(false);
		req.addHeader(new Http2Header(Http2HeaderName.CONNECTION, "keep-alive"));

		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		RequestStreamHandle requestStream = httpSocket.openStream();
		StreamRef ref = requestStream.process(req, mockListener);
		
		CancelReason reason = new RstStreamFrame();
		CompletableFuture<Void> cancelDone = ref.cancel(reason);
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertFalse(mockChannel.isClosed());
	}

	@Test
	public void testClientCancelNoKeepAlive() {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		Http2Request req = Requests.createRequest(false);
		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		RequestStreamHandle requestStream = httpSocket.openStream();
		StreamRef ref = requestStream.process(req, mockListener);
		
		CancelReason reason = new RstStreamFrame();
		CompletableFuture<Void> cancelDone = ref.cancel(reason);
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertTrue(mockChannel.isClosed());
	}

	@Test
	public void testServerCloseSocket() {
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		Http2Request req = Requests.createRequest(false);
		mockChannel.addWriteResponse(CompletableFuture.completedFuture(null));
		RequestStreamHandle requestStream = httpSocket.openStream();
		StreamRef ref = requestStream.process(req, mockListener);
		
		Assert.assertFalse(mockListener.isCancelled());
		
		mockChannel.simulateClose();
		
		Assert.assertTrue(mockListener.isCancelled());
	}
}