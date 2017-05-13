package org.webpieces.http2client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.util.RequestHolder;
import org.webpieces.http2client.util.Requests;
import org.webpieces.http2client.util.RequestsSent;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class TestCMaxConcurrentSetting extends AbstractTest {
	
	@Test
	public void testSend2ndRequestOnlyOnCompletionOfFirst() throws InterruptedException, ExecutionException {
		RequestsSent sent = sendTwoRequests();

		int streamId1 = sent.getRequest1().getRequest().getStreamId();
		CompletableFuture<StreamWriter> future2 = sent.getRequest2().getFuture();
		MockResponseListener listener1 = sent.getRequest1().getListener(); 
		
		sendHeadersAndData(streamId1, future2, listener1);
		
		listener1.getSingleReturnValueIncomingResponse();

		Http2Headers frame = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(sent.getRequest2().getRequest(), frame);
		Assert.assertTrue(future2.isDone());
	}
	
	@Test
	public void testSend2ndRequestOnlyOnAfterSettingsFrameMaxConcurrentBigger() throws InterruptedException, ExecutionException {
		RequestsSent sent = sendTwoRequests();

		Assert.assertFalse(sent.getRequest2().getFuture().isDone());

		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(2L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.write(new SettingsFrame(true)); //ack client frame
		List<Http2Msg> msgs = mockChannel.getFramesAndClear();
		
		Assert.assertEquals(sent.getRequest2().getRequest(), msgs.get(0));
		Assert.assertTrue(sent.getRequest2().getFuture().isDone());
		
		SettingsFrame ack = (SettingsFrame) msgs.get(1);
		Assert.assertEquals(true, ack.isAck());
	}
	
	private void sendHeadersAndData(int streamId1, CompletableFuture<StreamWriter> future2,
			MockResponseListener listener1) throws InterruptedException, ExecutionException {
		mockChannel.write(Requests.createResponse(streamId1)); //endOfStream=false
		listener1.getSingleReturnValueIncomingResponse();
				
		mockChannel.write(new DataFrame(streamId1, false)); //endOfStream=false
		listener1.getSingleReturnValueIncomingResponse();
		
		//at this point, should not have a call outstanding
		mockChannel.assertNoIncomingMessages();
		Assert.assertFalse(future2.isDone());

		listener1.addReturnValueIncomingResponse(CompletableFuture.completedFuture(null));
		Assert.assertFalse(future2.isDone());
		mockChannel.write(new DataFrame(streamId1, true));//endOfStream = true
	}
	
	private RequestsSent sendTwoRequests() {
		Http2Headers request1 = Requests.createRequest();
		Http2Headers request2 = Requests.createRequest();
		MockResponseListener listener1 = new MockResponseListener();
		MockResponseListener listener2 = new MockResponseListener();

		listener1.setIncomingRespDefault(CompletableFuture.completedFuture(null));
		CompletableFuture<StreamWriter> future = httpSocket.send(request1, listener1);
		CompletableFuture<StreamWriter> future2 = httpSocket.send(request2, listener2);
		
		RequestHolder r1 = new RequestHolder(request1, listener1, future);
		RequestHolder r2 = new RequestHolder(request2, listener2, future2);		
		RequestsSent requests = new RequestsSent(r1, r2);
		
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(1, req.getStreamId());
		Assert.assertEquals(request1, req);
		
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future2.isDone());
		return requests;
	}

	private void sendAndAckSettingsFrame(long max) throws InterruptedException, ExecutionException {
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(max);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.write(new SettingsFrame(true)); //ack client frame
		SettingsFrame ack = (SettingsFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(true, ack.isAck());
	}
}
