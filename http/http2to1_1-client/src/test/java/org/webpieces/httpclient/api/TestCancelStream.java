package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import org.webpieces.util.futures.XFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.TwoPools;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient.Http2CloseListener;
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

		mockChannel.setConnectFuture(XFuture.completedFuture(null));

		mockChanMgr.addTCPChannelToReturn(mockChannel);
		httpSocket = httpClient.createHttpSocket(new Http2CloseListener());
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
		XFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		Http2Request req = Requests.createRequest(false);
		req.addHeader(new Http2Header(Http2HeaderName.CONNECTION, "keep-alive"));

		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		RequestStreamHandle requestStream = httpSocket.openStream();
		StreamRef ref = requestStream.process(req, mockListener);
		
		CancelReason reason = new RstStreamFrame();
		XFuture<Void> cancelDone = ref.cancel(reason);
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertFalse(mockChannel.isClosed());
	}

	@Test
	public void testClientCancelNoKeepAlive() {
		XFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		Http2Request req = Requests.createRequest(false);
		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		RequestStreamHandle requestStream = httpSocket.openStream();
		StreamRef ref = requestStream.process(req, mockListener);
		
		CancelReason reason = new RstStreamFrame();
		XFuture<Void> cancelDone = ref.cancel(reason);
		Assert.assertTrue(cancelDone.isDone());
		
		//Assert the socket is NOT closed
		Assert.assertTrue(mockChannel.isClosed());
	}

	@Test
	public void testServerCloseSocket() {
		XFuture<Void> connect = httpSocket.connect(new InetSocketAddress(8555));
		MockResponseListener mockListener = new MockResponseListener();
		
		Http2Request req = Requests.createRequest(false);
		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		RequestStreamHandle requestStream = httpSocket.openStream();
		StreamRef ref = requestStream.process(req, mockListener);
		
		Assert.assertFalse(mockListener.isCancelled());
		
		mockChannel.simulateClose();
		
		Assert.assertTrue(mockListener.isCancelled());
	}
}