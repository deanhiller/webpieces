package org.webpieces.http2client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.mock.MockPushListener;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockStreamWriter;
import org.webpieces.http2client.mock.TestAssert;
import org.webpieces.http2client.util.Requests;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.GoAwayFrame;
import com.webpieces.http2.api.dto.lowlevel.PriorityFrame;
import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2ErrorCode;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.PriorityDetails;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.api.error.ConnectionClosedException;
import com.webpieces.http2engine.api.error.ShutdownStream;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class TestC5x1StreamStates extends AbstractTest {
	
	/**
	 * Receiving any frame other than HEADERS or PRIORITY on a stream in this state
	 *  MUST be treated as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1BadFrameReceivedInIdleState() {
		DataFrame dataFrame = new DataFrame(1, false);
		mockChannel.write(dataFrame); //endOfStream=false

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: MockHttp2Channel1:stream1:(BAD_FRAME_RECEIVED_FOR_THIS_STATE) "
				+ "Stream in idle state and received this frame which should not happen in "
				+ "idle state.  frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		//send new request on closed connection
		MockResponseListener listener1 = new MockResponseListener();
		Http2Request request1 = Requests.createRequest();
		StreamRef streamRef = httpSocket.openStream().process(request1, listener1);
		CompletableFuture<StreamWriter> future = streamRef.getWriter();

		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
	}
	
	/**
	 * Receiving any type of frame other than HEADERS, RST_STREAM, or PRIORITY on a
	 * stream in this state MUST be treated as a connection 
	 * error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1BadFrameReceivedInReservedRemoteState() {
		MockPushListener pushListener = new MockPushListener();
		pushListener.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(null));
		listener1.addReturnValuePush(pushListener);
		
		Http2Request request = sendRequestToServer(listener1);
		Http2Push push = sendPushFromServer(listener1, request);
		
		DataFrame dataFrame = new DataFrame(push.getPromisedStreamId(), false);
		mockChannel.write(dataFrame);

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: MockHttp2Channel1:stream2:(BAD_FRAME_RECEIVED_FOR_THIS_STATE) "
				+ "No transition defined on statemachine for event=RECV_DATA when in state=Reserved(remote)", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		ShutdownStream failResp1 = (ShutdownStream) listener1.getSingleRstStream();
		Assert.assertEquals(CancelReasonCode.BAD_FRAME_RECEIVED_FOR_THIS_STATE, failResp1.getCause().getReasonCode());
		
		ShutdownStream failResp2 = (ShutdownStream) listener1.getSingleCancelPush();
		Assert.assertEquals(CancelReasonCode.BAD_FRAME_RECEIVED_FOR_THIS_STATE, failResp2.getCause().getReasonCode());
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
		MockStreamWriter mockWriter = new MockStreamWriter();
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(mockWriter));
		Http2Request request = sendRequestToServer(listener1);
		sendResetFromServer(listener1, request);

		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		mockChannel.write(dataFrame);
		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.STREAM_CLOSED, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: MockHttp2Channel1:stream1:(CLOSED_STREAM) "
				+ "Stream must have been closed as it no longer exists.  high mark=1  "
				+ "your frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());
		
		//send new request on closed connection
		Http2Request request1 = Requests.createRequest();
		StreamRef streamRef = httpSocket.openStream().process(request1, listener1);
		CompletableFuture<StreamWriter> future = streamRef.getWriter();

		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
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
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(null));
		Http2Request request = sendRequestToServer(listener1);
		
		sendEosResponseFromServer(listener1, request);

		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		mockChannel.write(dataFrame);
		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.STREAM_CLOSED, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: MockHttp2Channel1:stream1:"
				+ "(CLOSED_STREAM) Stream must have been closed as it no longer exists.  "
				+ "high mark=1  your frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());
		
		//send new request on closed connection
		Http2Request request1 = Requests.createRequest();
		StreamRef streamRef = httpSocket.openStream().process(request1, listener1);
		CompletableFuture<StreamWriter> future = streamRef.getWriter();

		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
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
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(null));
		Http2Request request = sendRequestToServer(listener1);
		sendResetFromServer(listener1, request);

		PriorityDetails details = new PriorityDetails();
		details.setStreamDependency(3);
		PriorityFrame dataFrame = new PriorityFrame(request.getStreamId(), details);
		mockChannel.write(dataFrame);
		
		//priority is ignored
		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
		Assert.assertFalse(mockChannel.isClosed());
		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());		
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
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(null));

		Http2Request request1 = Requests.createRequest();
		RequestStreamHandle stream = httpSocket.openStream();
		StreamRef streamRef = httpSocket.openStream().process(request1, listener1);
		CompletableFuture<StreamWriter> future = streamRef.getWriter();

		@SuppressWarnings("unused")
		StreamWriter writer = future.get(2, TimeUnit.SECONDS);
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(request1, req);
		
		RstStreamFrame rst = new RstStreamFrame(request1.getStreamId(), Http2ErrorCode.CANCEL);
		CompletableFuture<Void> cancel = streamRef.cancel(rst);
		cancel.get(2, TimeUnit.SECONDS);
		
		Http2Msg svrRst = mockChannel.getFrameAndClear();
		Assert.assertEquals(rst, svrRst);
		
		//simulate server responding before receiving the cancel
		Http2Response resp1 = Requests.createEosResponse(request1.getStreamId());
		mockChannel.write(resp1); //endOfStream=true

//		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
//		Assert.assertFalse(mockChannel.isClosed());
//		
//		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());
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
	
	private Http2Push sendPushFromServer(MockResponseListener listener1, Http2Request request) {
		Http2Push resp1 = Requests.createPush(request.getStreamId());
		mockChannel.write(resp1); 
		Http2Push response1 = listener1.getSinglePush();
		Assert.assertEquals(resp1, response1);
		return resp1;
	}

	private void sendResetFromServer(MockResponseListener listener1, Http2Request request) {
		RstStreamFrame resp1 = Requests.createReset(request.getStreamId());
		mockChannel.write(resp1); //endOfStream=true
		RstStreamFrame response1 = (RstStreamFrame) listener1.getSingleRstStream();
		Assert.assertEquals(resp1, response1);
	}
	
	private void sendEosResponseFromServer(MockResponseListener listener1, Http2Request request) {
		Http2Response resp1 = Requests.createEosResponse(request.getStreamId());
		mockChannel.write(resp1); //endOfStream=true
		Http2Response response1 = (Http2Response) listener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(resp1, response1);
	}


}
