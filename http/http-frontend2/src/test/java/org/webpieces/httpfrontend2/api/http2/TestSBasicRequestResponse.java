package org.webpieces.httpfrontend2.api.http2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class TestSBasicRequestResponse extends AbstractHttp2Test {

	@Test
	public void testWithNoData() throws InterruptedException, ExecutionException, TimeoutException {
		Http2Headers request1 = Http2Requests.createRequest(1, true);
		mockChannel.send(request1);		
		PassedIn incoming = mockListener.getSingleRequest();
		Assert.assertEquals(request1, incoming.request);
		
		Http2Headers resp = Http2Requests.createResponse(request1.getStreamId(), true);
		
		incoming.stream.sendResponse(resp);
		
		Http2Msg response = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, response);
	}
	
	@Test
	public void testWithData() throws InterruptedException, ExecutionException, TimeoutException {
        MockStreamWriter mockSw = new MockStreamWriter();
        mockSw.setDefaultRetValToThis();
		mockListener.addMockStreamToReturn(mockSw );

		Http2Headers request1 = Http2Requests.createRequest(1, false);
		DataFrame data = Http2Requests.createData1(request1.getStreamId(), true);

		mockChannel.send(request1);
		PassedIn incoming1 = mockListener.getSingleRequest();
		Assert.assertEquals(request1, incoming1.request);

		mockChannel.send(data);
		DataFrame incoming = (DataFrame) mockSw.getSingleFrame();
		Assert.assertEquals(3, incoming.getData().getReadableSize());

		//clear window update frames
		Assert.assertEquals(2, mockChannel.getFramesAndClear().size());

		Http2Headers resp = Http2Requests.createResponse(request1.getStreamId(), false);
		CompletableFuture<StreamWriter> future = incoming1.stream.sendResponse(resp);
		
		Http2Msg response = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, response);
		
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		DataFrame data2 = Http2Requests.createData2(request1.getStreamId(), true);
		writer.send(data2);
		DataFrame dataResp = (DataFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(1, dataResp.getData().getReadableSize());
		
	}
	
	@Test
	public void testWithDataAndTrailingHeaders() throws InterruptedException, ExecutionException, TimeoutException {
        MockStreamWriter mockSw = new MockStreamWriter();
        mockSw.setDefaultRetValToThis();
		mockListener.addMockStreamToReturn(mockSw );

		Http2Headers request1 = Http2Requests.createRequest(1, false);
		DataFrame data1 = Http2Requests.createData1(request1.getStreamId(), false);
		Http2Headers trailing = Http2Requests.createTrailers(request1.getStreamId());

		mockChannel.send(request1);
		PassedIn incoming1 = mockListener.getSingleRequest();
		Assert.assertEquals(request1, incoming1.request);

		mockChannel.send(data1);
		DataFrame incoming2 = (DataFrame) mockSw.getSingleFrame();
		Assert.assertEquals(3, incoming2.getData().getReadableSize());
		
		//clear window update frames
		Assert.assertEquals(2, mockChannel.getFramesAndClear().size());
		
		mockChannel.send(trailing);
		Http2Headers incoming = (Http2Headers) mockSw.getSingleFrame();
		Assert.assertEquals(trailing, incoming);		
		
		Http2Headers resp = Http2Requests.createResponse(request1.getStreamId(), false);
		CompletableFuture<StreamWriter> future = incoming1.stream.sendResponse(resp);
		
		Http2Msg response = mockChannel.getFrameAndClear();
		Assert.assertEquals(resp, response);
		
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		DataFrame data2 = Http2Requests.createData2(request1.getStreamId(), false);
		writer.send(data2);
		DataFrame dataResp = (DataFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(1, dataResp.getData().getReadableSize());		
		
		Http2Headers trailingResp = Http2Requests.createTrailers(request1.getStreamId());
		writer.send(trailingResp);
		
		Http2Headers trailers = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(trailingResp, trailers);
	}
	
	
	@Test
	public void testPushPromise() throws InterruptedException, ExecutionException, TimeoutException {
		Http2Headers request1 = Http2Requests.createRequest(1, true);
		mockChannel.send(request1);		
		PassedIn incoming = mockListener.getSingleRequest();
		Assert.assertEquals(request1, incoming.request);
		
		Http2Push push = Http2Requests.createPush(request1.getStreamId());
		CompletableFuture<StreamWriter> future = incoming.stream.sendPush(push);
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		
		Http2Push pushRecv = (Http2Push) mockChannel.getFrameAndClear();
		Assert.assertEquals(push, pushRecv);
		
		Http2Headers preEmptive = Http2Requests.createResponse(push.getPromisedStreamId());
		writer.send(preEmptive);

		Http2Headers preEmptRecv = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(preEmptive, preEmptRecv);
		
		Http2Headers response = Http2Requests.createResponse(request1.getStreamId());
		incoming.stream.sendResponse(response);
		
		Http2Headers responseRecv = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(response, responseRecv);
	}
}
