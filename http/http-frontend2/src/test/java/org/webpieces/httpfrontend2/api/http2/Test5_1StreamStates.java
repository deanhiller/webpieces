package org.webpieces.httpfrontend2.api.http2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class Test5_1StreamStates extends AbstractHttp2Test {
	
	/**
	 * Receiving any frame other than HEADERS or PRIORITY on a stream in this state
	 *  MUST be treated as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1BadFrameReceivedInIdleState() {
		DataFrame dataFrame = new DataFrame(1, false);
		mockChannel.write(dataFrame); //endOfStream=false

		//no request comes in
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		//no cancels
		Assert.assertEquals(0, mockListener.getNumCancelsThatCameIn());
		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("Stream in idle state and received this frame which should not happen in idle state.  "
				+ "frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}  reason=BAD_FRAME_RECEIVED_FOR_THIS_STATE stream=1", msg);
		Assert.assertTrue(mockChannel.isClosed());
	}
	
	/**
	 * Receiving any type of frame other than HEADERS, RST_STREAM, or PRIORITY on a
	 * stream in this state MUST be treated as a connection 
	 * error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1BadFrameReceivedInReservedRemoteState() {
//		MockPushListener pushListener = new MockPushListener();
//		pushListener.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		listener1.addReturnValuePush(pushListener);
//		
//		Http2Headers request = sendRequestToServer(listener1);
//		Http2Push svrPush = sendPushFromServer(listener1, request);
//		
//		Http2Push push = (Http2Push) pushListener.getSingleParam();
//		Assert.assertEquals(svrPush, push);
//		
//		DataFrame dataFrame = new DataFrame(push.getPromisedStreamId(), false);
//		mockChannel.write(dataFrame);
//
//		//remote receives goAway
//		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
//		DataWrapper debugData = goAway.getDebugData();
//		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
//		Assert.assertEquals("No transition defined on statemachine for event=Http2Event "
//				+ "[sendReceive=RECEIVE, payloadType=DATA] when in state=Reserved(remote) "
//				+ "reason=BAD_FRAME_RECEIVED_FOR_THIS_STATE stream=2", msg);
//		Assert.assertTrue(mockChannel.isClosed());
//		
//		ConnectionReset failResp1 = (ConnectionReset) listener1.getSingleReturnValueIncomingResponse();
//		Assert.assertEquals(ParseFailReason.BAD_FRAME_RECEIVED_FOR_THIS_STATE, failResp1.getReason().getReason());
//		
//		ConnectionReset failResp2 = (ConnectionReset) pushListener.getSingleParam();
//		Assert.assertEquals(ParseFailReason.BAD_FRAME_RECEIVED_FOR_THIS_STATE, failResp2.getReason().getReason());
	}

	/**
	 * An endpoint MUST NOT send frames other than PRIORITY on a closed stream. An endpoint 
	 * that receives any frame other than PRIORITY after receiving a ----RST_STREAM---- MUST 
	 * treat that as a stream error (Section 5.4.2) of type STREAM_CLOSED. Similarly, an 
	 * endpoint that receives any frames after receiving a frame with the 
	 * END_STREAM flag set MUST treat that as a connection error (Section 5.4.1) of 
	 * type STREAM_CLOSED, unless the frame is permitted as described below.
	 * 
	 */
	@Test
	public void testSection5_1ReceiveBadFrameAfterReceiveRstStreamFrame() {	
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		Http2Headers request = sendRequestToServer(listener1);
//		sendResetFromServer(listener1, request);
//
//		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
//		mockChannel.write(dataFrame);
//		
//		//remote receives goAway
//		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.STREAM_CLOSED, goAway.getKnownErrorCode());
//		DataWrapper debugData = goAway.getDebugData();
//		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
//		Assert.assertEquals("Stream must have been closed as it no longer exists.  high mark=1  "
//				+ "your frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}  reason=CLOSED_STREAM stream=1", msg);
//		Assert.assertTrue(mockChannel.isClosed());
//		
//		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());
//		
//		//send new request on closed connection
//		Http2Headers request1 = Requests.createRequest();
//		CompletableFuture<StreamWriter> future = httpSocket.send(request1, listener1);
//		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
//		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
//		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
	}
	
	/**
	 * An endpoint MUST NOT send frames other than PRIORITY on a closed stream. An endpoint 
	 * that receives any frame other than PRIORITY after receiving a RST_STREAM MUST 
	 * treat that as a stream error (Section 5.4.2) of type STREAM_CLOSED. Similarly, an 
	 * endpoint that receives any frames after receiving a frame with the 
	 * -----END_STREAM flag---- set MUST treat that as a connection error (Section 5.4.1) of 
	 * type STREAM_CLOSED, unless the frame is permitted as described below.
	 * 
	 */
	@Test
	public void testSection5_1ReceiveBadFrameAfterReceiveEndStream() {	
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		Http2Headers request = sendRequestToServer(listener1);
//		sendEosResponseFromServer(listener1, request);
//
//		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
//		mockChannel.write(dataFrame);
//		
//		//remote receives goAway
//		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.STREAM_CLOSED, goAway.getKnownErrorCode());
//		DataWrapper debugData = goAway.getDebugData();
//		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
//		Assert.assertEquals("Stream must have been closed as it no longer exists.  high mark=1  "
//				+ "your frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}  reason=CLOSED_STREAM stream=1", msg);
//		Assert.assertTrue(mockChannel.isClosed());
//		
//		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());
//		
//		//send new request on closed connection
//		Http2Headers request1 = Requests.createRequest();
//		CompletableFuture<StreamWriter> future = httpSocket.send(request1, listener1);
//		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
//		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
//		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
	}

	/**
	 * An endpoint MUST NOT send frames other than ----PRIORITY---- on a closed stream. An endpoint 
	 * that receives any frame other than ----PRIORITY---- after receiving a ----RST_STREAM---- MUST 
	 * treat that as a stream error (Section 5.4.2) of type STREAM_CLOSED. Similarly, an 
	 * endpoint that receives any frames after receiving a frame with the 
	 * END_STREAM flag set MUST treat that as a connection error (Section 5.4.1) of 
	 * type STREAM_CLOSED, unless the frame is permitted as described below.
	 * 
	 */
	@Test
	public void testSection5_1ReceivePriorityAfterReceiveRstStreamFrame() {	
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		Http2Headers request = sendRequestToServer(listener1);
//		sendResetFromServer(listener1, request);
//
//		PriorityDetails details = new PriorityDetails();
//		details.setStreamDependency(3);
//		PriorityFrame dataFrame = new PriorityFrame(request.getStreamId(), details);
//		mockChannel.write(dataFrame);
//		
//		//priority is ignored
//		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
//		Assert.assertFalse(mockChannel.isClosed());
//		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());		
	}
	
	/**
	 * If this state is reached as a result of sending a RST_STREAM frame, the 
	 * peer that receives the RST_STREAM might have already sent — or enqueued for 
	 * sending — frames on the stream that cannot be withdrawn. An endpoint MUST ignore 
	 * frames that it receives on closed streams after it has sent a RST_STREAM frame. An 
	 * endpoint MAY choose to limit the period over which it ignores frames and 
	 * treat frames that arrive after this time as being in error.
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testSection5_1ReceiveValidFramesAfterSendRstStreamFrame() throws InterruptedException, ExecutionException, TimeoutException {	
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//
//		Http2Headers request1 = Requests.createRequest();
//		CompletableFuture<StreamWriter> future = httpSocket.send(request1, listener1);
//		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
//		Http2Msg req = mockChannel.getFrameAndClear();
//		Assert.assertEquals(request1, req);
//		
//		RstStreamFrame rst = new RstStreamFrame(request1.getStreamId(), Http2ErrorCode.CANCEL);
//		writer.send(rst);
//		
//		Http2Msg svrRst = mockChannel.getFrameAndClear();
//		Assert.assertEquals(rst, svrRst);
//		
//		//simulate server responding before receiving the cancel
//		Http2Headers resp1 = Requests.createEosResponse(request1.getStreamId());
//		mockChannel.write(resp1); //endOfStream=true
//
////		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
////		Assert.assertFalse(mockChannel.isClosed());
////		
////		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());
	}
	
	/**
	 * If this state is reached as a result of sending a RST_STREAM frame, the 
	 * peer that receives the RST_STREAM might have already sent — or enqueued for 
	 * sending — frames on the stream that cannot be withdrawn. An endpoint MUST ignore 
	 * frames that it receives on closed streams after it has sent a RST_STREAM frame. An 
	 * endpoint MAY choose to limit the period over which it ignores frames and 
	 * treat frames that arrive after this time as being in error.
	 */
	@Test
	public void testSection5_1ReceiveVeryDelayedFrameAfterSendingRstFrame() {	
		
	}
	
//	private Http2Push sendPushFromServer(MockResponseListener listener1, Http2Headers request) {
//		Http2Push resp1 = Requests.createPush(request.getStreamId());
//		mockChannel.write(resp1); 
//		int response1 = listener1.getSinglePushStreamId();
//		Assert.assertEquals(resp1.getPromisedStreamId(), response1);
//		return resp1;
//	}
//
//	private void sendResetFromServer(MockResponseListener listener1, Http2Headers request) {
//		RstStreamFrame resp1 = Requests.createReset(request.getStreamId());
//		mockChannel.write(resp1); //endOfStream=true
//		RstStreamFrame response1 = (RstStreamFrame) listener1.getSingleReturnValueIncomingResponse();
//		Assert.assertEquals(resp1, response1);
//	}
//	
//	private void sendEosResponseFromServer(MockResponseListener listener1, Http2Headers request) {
//		Http2Headers resp1 = Requests.createEosResponse(request.getStreamId());
//		mockChannel.write(resp1); //endOfStream=true
//		Http2Headers response1 = (Http2Headers) listener1.getSingleReturnValueIncomingResponse();
//		Assert.assertEquals(resp1, response1);
//	}


}
