package org.webpieces.http2client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockStreamWriter;
import org.webpieces.http2client.util.Requests;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;

public class TestBasicHttp2Client extends AbstractTest {
	
	@Test
	public void testMaxConcurrentOne() throws InterruptedException, ExecutionException {
		Http2Request request1 = Requests.createRequest();
		Http2Request request2 = Requests.createRequest();

		MockStreamWriter writer1 = new MockStreamWriter();
		MockResponseListener respListener1 = new MockResponseListener();
		respListener1.setIncomingRespDefault(CompletableFuture.completedFuture(writer1));
		MockResponseListener respListener2 = new MockResponseListener();
		CompletableFuture<StreamWriter> future = httpSocket.openStream().process(request1, respListener1);
		CompletableFuture<StreamWriter> future2 = httpSocket.openStream().process(request2, respListener2);
		
		//max concurrent only 1 so only get 1
		Http2Request req = (Http2Request) mockChannel.getFrameAndClear();
		Assert.assertEquals(1, req.getStreamId());
		Assert.assertEquals(request1, req);
		
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future2.isDone()); //do not ack upstream until out the door(backpressure)
		
		Http2Response resp1 = Requests.createResponse(request1.getStreamId());
		mockChannel.write(resp1); //endOfStream=false
		Http2Response response1 = respListener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(resp1, response1);
		
		Assert.assertFalse(future2.isDone());
		mockChannel.write(new DataFrame(request1.getStreamId(), false)); //endOfStream=false
		writer1.getSingleFrame();
		
		//at this point, should not have a call outstanding
		mockChannel.assertNoIncomingMessages();
				
		//WRITE OUT END STREAM data so the first request starts going again!!
		Assert.assertFalse(future2.isDone());
		DataFrame dataLast = new DataFrame(request1.getStreamId(), true);
		mockChannel.write(dataLast);//endOfStream = true
		Assert.assertTrue(future2.isDone());
		
		DataFrame data = (DataFrame) writer1.getSingleFrame();
		Assert.assertEquals(dataLast.getStreamId(), data.getStreamId());
		
		Http2Request req2 = (Http2Request) mockChannel.getFrameAndClear();
		Assert.assertEquals(request2, req2);
	}

	@Test
	public void testBasicSendRespond() {
		FullRequest request1 = Requests.createHttp2Request();

		MockResponseListener respListener1 = new MockResponseListener();
		respListener1.setIncomingRespDefault(CompletableFuture.completedFuture(null));
		CompletableFuture<FullResponse> future = httpSocket.send(request1);
		
		Assert.assertFalse(future.isDone());
		
	}
	
}
