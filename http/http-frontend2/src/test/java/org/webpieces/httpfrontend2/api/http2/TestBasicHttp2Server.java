package org.webpieces.httpfrontend2.api.http2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;

public class TestBasicHttp2Server extends AbstractHttp2Test {
	
	@Test
	public void testBasicIntegration() throws InterruptedException, ExecutionException {
        MockStreamWriter mockSw = new MockStreamWriter();
		mockListener.addMockStreamToReturn(mockSw );
        MockStreamWriter mockSw2 = new MockStreamWriter();
		mockListener.addMockStreamToReturn(mockSw2 );
		
		Http2Request request1 = Http2Requests.createRequest(1, true);
		Http2Request request2 = Http2Requests.createRequest(3, true);

		mockChannel.send(request1);
		PassedIn requestAndStream1 = mockListener.getSingleRequest();
		mockChannel.send(request2);
		PassedIn requestAndStream2 = mockListener.getSingleRequest();
		
		//each stream given to webapp is a unique one....
		Assert.assertTrue(requestAndStream1.stream != requestAndStream2.stream);
		
		Assert.assertEquals(request1, requestAndStream1.request);
		Assert.assertEquals(request2, requestAndStream2.request);
		Assert.assertEquals(1, request1.getStreamId());
		Assert.assertEquals(3, request2.getStreamId());
		
		Http2Response resp2 = Http2Requests.createResponse(request2.getStreamId());
		CompletableFuture<StreamWriter> future = requestAndStream2.stream.sendResponse(resp2);
		Assert.assertTrue(future.isDone());

		Http2Response frame2 = (Http2Response) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp2, frame2);

		Http2Response resp1 = Http2Requests.createResponse(request1.getStreamId());
		CompletableFuture<StreamWriter> future1 = requestAndStream1.stream.sendResponse(resp1);
		Assert.assertTrue(future1.isDone());

		Http2Response frame1 = (Http2Response) mockChannel.getFrameAndClear();
		Assert.assertEquals(resp1, frame1);		
	}
	
	@Test
	public void testBasicUpload() {
		
	}
	
	@Test
	public void testBasicDownload() {
		
	}
	
	@Test
	public void testBasicPushResponse() {
		
	}
}
