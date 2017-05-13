package org.webpieces.http2client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.http2client.api.dto.Http2Request;
import org.webpieces.http2client.api.dto.Http2Response;
import org.webpieces.http2client.mock.MockPushListener;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.util.Requests;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class TestCBasicRequestResponse extends AbstractTest {

	@Test
	public void testWithNoData() throws InterruptedException, ExecutionException, TimeoutException {
		Http2Request request1 = new Http2Request();
		request1.setHeaders(Requests.createRequest());
		
		CompletableFuture<Http2Response> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
		Http2Headers frame = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(1, frame.getStreamId());
		
		Http2Headers resp = Requests.createResponse(request1.getHeaders().getStreamId());
		resp.setEndOfStream(true);
		mockChannel.write(resp);

		Http2Response response = future.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(0, response.getPayload().getReadableSize());		
	}
	
	@Test
	public void testWithData() throws InterruptedException, ExecutionException, TimeoutException {
		Http2Request request1 = Requests.createHttp2Request();

		CompletableFuture<Http2Response> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(2, frames.size());
		
		Http2Headers resp = Requests.createResponse(request1.getHeaders().getStreamId());
		mockChannel.write(resp);
		
		Assert.assertFalse(future.isDone());

		DataFrame data = Requests.createData(request1.getHeaders().getStreamId());
		mockChannel.write(data);

		Http2Response response = future.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(2, response.getPayload().getReadableSize());
	}
	
	@Test
	public void testWithDataAndTrailingHeaders() throws InterruptedException, ExecutionException, TimeoutException {
		Http2Request request1 = Requests.createHttp2Request();
		Http2Headers trailing = Requests.createRequest();
		request1.setTrailingHeaders(trailing);

		CompletableFuture<Http2Response> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(3, frames.size());
		
		Http2Headers resp = Requests.createResponse(request1.getHeaders().getStreamId());
		mockChannel.write(resp);
		
		Assert.assertFalse(future.isDone());

		DataFrame data = Requests.createData(request1.getHeaders().getStreamId());
		data.setEndOfStream(false);
		mockChannel.write(data);
		
		Assert.assertFalse(future.isDone());

		mockChannel.write(trailing);

		Http2Response response = future.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(2, response.getPayload().getReadableSize());
		Assert.assertNotNull(response.getTrailingHeaders());
	}
	
	@Test
	public void testPushPromise() {
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
		Http2Headers request = sendRequestToServer(listener1);
		
		MockPushListener pushListener = new MockPushListener();
		sendPushPromise(listener1, pushListener, request.getStreamId(), true);
		sendResponseFromServer(listener1, request);
		
	}
}
