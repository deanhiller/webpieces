package org.webpieces.http2client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.util.Requests;

import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class TestCBasicRequestResponse extends AbstractTest {

	@Test
	public void testWithNoData() throws InterruptedException, ExecutionException, TimeoutException {
		FullRequest request1 = new FullRequest();
		request1.setHeaders(Requests.createRequest());
		
		CompletableFuture<FullResponse> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
		Http2Headers frame = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(1, frame.getStreamId());
		
		Http2Response resp = Requests.createResponse(request1.getHeaders().getStreamId());
		resp.setEndOfStream(true);
		mockChannel.write(resp);

		FullResponse response = future.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(0, response.getPayload().getReadableSize());		
	}
	
	@Test
	public void testWithData() throws InterruptedException, ExecutionException, TimeoutException {
		FullRequest request1 = Requests.createHttp2Request();

		CompletableFuture<FullResponse> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(2, frames.size());
		
		Http2Response resp = Requests.createResponse(request1.getHeaders().getStreamId());
		mockChannel.write(resp);
		
		Assert.assertFalse(future.isDone());

		DataFrame data = Requests.createData(request1.getHeaders().getStreamId(), true);
		mockChannel.write(data);

		FullResponse response = future.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(2, response.getPayload().getReadableSize());
	}
	
	@Test
	public void testWithDataAndTrailingHeaders() throws InterruptedException, ExecutionException, TimeoutException {
		FullRequest request1 = Requests.createHttp2Request();
		Http2Trailers trailing = Requests.createTrailers();
		request1.setTrailingHeaders(trailing);

		CompletableFuture<FullResponse> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(3, frames.size());
		
		Http2Response resp = Requests.createResponse(request1.getHeaders().getStreamId());
		mockChannel.write(resp);
		
		Assert.assertFalse(future.isDone());

		DataFrame data = Requests.createData(request1.getHeaders().getStreamId(), false);
		mockChannel.write(data);
		
		Assert.assertFalse(future.isDone());

		mockChannel.write(trailing);

		FullResponse response = future.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(2, response.getPayload().getReadableSize());
		Assert.assertNotNull(response.getTrailingHeaders());
	}
	
	@Test
	public void testPushPromise() {
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(null));
		Http2Request request = sendRequestToServer(listener1);
		
		sendPushPromise(listener1, request.getStreamId(), true);
		sendResponseFromServer(listener1, request);
		
	}
}
