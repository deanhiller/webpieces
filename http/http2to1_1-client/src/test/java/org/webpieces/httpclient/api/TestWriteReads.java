package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.TwoPools;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.httpclient.Http2CloseListener;
import org.webpieces.httpclient.api.mocks.MockChannel;
import org.webpieces.httpclient.api.mocks.MockChannelMgr;
import org.webpieces.httpclient.api.mocks.MockResponseListener;
import org.webpieces.httpclientx.api.Http2to11ClientFactory;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestWriteReads {

	private MockChannelMgr mockChannelMgr = new MockChannelMgr();
	private MockChannel mockChannel = new MockChannel();
	private Http2Client httpClient;
	private Http2Socket socket;

	@Before
	public void setup() throws InterruptedException, ExecutionException, TimeoutException {
		BufferPool pool = new TwoPools("pl", new SimpleMeterRegistry());
		httpClient = Http2to11ClientFactory.createHttpClient("myClient4", mockChannelMgr, new SimpleMeterRegistry(), pool);
		
		mockChannelMgr.addTCPChannelToReturn(mockChannel);
		socket = httpClient.createHttpSocket(new Http2CloseListener());

		mockChannel.setConnectFuture(XFuture.completedFuture(null));
		XFuture<Void> future = socket.connect(new InetSocketAddress(8080));
		
		future.get(2, TimeUnit.SECONDS);
	}

	@Test
	public void testBasicReadWrite() throws InterruptedException, ExecutionException, TimeoutException {
		MockResponseListener listener = new MockResponseListener();
		RequestStreamHandle handle = socket.openStream();

		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		Http2Request request = Requests.createRequest();
		StreamRef streamRef = handle.process(request, listener);
		XFuture<StreamWriter> writer = streamRef.getWriter();

		Assert.assertTrue(writer.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		MockResponseListener listener2 = new MockResponseListener();
		request.getHeaderLookupStruct().getHeader("serverid").setValue("2");
		mockChannel.addWriteResponse(XFuture.completedFuture(null));
		StreamRef streamRef1 = handle.process(request, listener2);
		XFuture<StreamWriter> writer2 = streamRef1.getWriter();

		Assert.assertTrue(writer2.isDone());
		Assert.assertEquals(request, mockChannel.getLastWriteParam());

		Http2Response response1 = Requests.createResponse(1, 0);
		listener.addProcessResponse(XFuture.completedFuture(null));
		XFuture<Void> fut1 = mockChannel.writeResponse(response1);
		fut1.get(2, TimeUnit.SECONDS); //throws if exception occurred and ensures future resolved
		
		Http2Response msg = listener.getIncomingMsg();
		Assert.assertEquals(response1, msg);
		
		Http2Response response2 = Requests.createResponse(2, 0);
		listener2.addProcessResponse(XFuture.completedFuture(null));
		XFuture<Void> fut2 = mockChannel.writeResponse(response2);
		fut2.get(2, TimeUnit.SECONDS); //throws if exception occurred and ensures future resolved
		Http2Response msg2 = listener2.getIncomingMsg();
		Assert.assertEquals(response2, msg2);
	}

}
