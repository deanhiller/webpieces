package org.webpieces.http2client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.http2client.api.dto.Http2Request;
import org.webpieces.http2client.api.dto.Http2Response;
import org.webpieces.http2client.mock.MockResponseListener;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class TestBasicHttp2Client extends AbstractTest {
	
	@Test
	public void testMaxConcurrentOne() throws InterruptedException, ExecutionException {
		Http2Headers request1 = Requests.createRequest();
		Http2Headers request2 = Requests.createRequest();

		MockResponseListener respListener1 = new MockResponseListener();
		respListener1.setIncomingRespDefault(CompletableFuture.completedFuture(null));
		MockResponseListener respListener2 = new MockResponseListener();
		CompletableFuture<StreamWriter> future = httpSocket.send(request1, respListener1);
		CompletableFuture<StreamWriter> future2 = httpSocket.send(request2, respListener2);
		
		//max concurrent only 1 so only get 1
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(1, req.getStreamId());
		Assert.assertEquals(request1, req);
		
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future2.isDone());
		
		Http2Headers resp1 = Requests.createResponse(request1.getStreamId());
		mockChannel.write(resp1); //endOfStream=false
		PartialStream response1 = respListener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(resp1, response1);
		
		Assert.assertFalse(future2.isDone());
		mockChannel.write(new DataFrame(request1.getStreamId(), false)); //endOfStream=false
		respListener1.getSingleReturnValueIncomingResponse();
		
		//at this point, should not have a call outstanding
		mockChannel.assertNoIncomingMessages();
				
		Assert.assertFalse(future2.isDone());
		mockChannel.write(new DataFrame(request1.getStreamId(), true));//endOfStream = true
		Assert.assertTrue(future2.isDone());
		
		respListener1.getSingleReturnValueIncomingResponse();
		
		Http2Msg frame = mockChannel.getFrameAndClear();
		Assert.assertEquals(3, frame.getStreamId());
	}

	@Test
	public void testBasicSendRespond() {
		Http2Request request1 = Requests.createHttp2Request();

		MockResponseListener respListener1 = new MockResponseListener();
		respListener1.setIncomingRespDefault(CompletableFuture.completedFuture(null));
		CompletableFuture<Http2Response> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
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
