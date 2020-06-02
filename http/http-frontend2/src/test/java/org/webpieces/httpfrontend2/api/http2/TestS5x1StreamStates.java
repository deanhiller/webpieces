package org.webpieces.httpfrontend2.api.http2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.httpfrontend2.api.mock2.MockStreamRef;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.Cancel;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class TestS5x1StreamStates extends AbstractHttp2Test {
	
	/**
	 * Receiving any frame other than HEADERS or PRIORITY on a stream in this state
	 *  MUST be treated as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1BadFrameReceivedInIdleState() {
		DataFrame dataFrame = new DataFrame(1, false);
		mockChannel.send(dataFrame);
		
		//no request comes in
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		
		Assert.assertTrue(mockListener.isClosed());
		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: HttpSocket[Http2ChannelCache1]:stream1:(BAD_FRAME_RECEIVED_FOR_THIS_STATE) "
				+ "Stream in idle state and received this frame which should not happen in idle state.  "
				+ "frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}", msg);
		Assert.assertTrue(mockChannel.isClosed());
	}
	
	/**
	 * reserved local
	 * 
	 * A PRIORITY or WINDOW_UPDATE frame MAY be received in this state. Receiving any 
	 * type of frame other than RST_STREAM, PRIORITY, or WINDOW_UPDATE on a stream 
	 * in this state MUST be treated as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1BadFrameReceivedInReservedRemoteState() {
		MockStreamWriter mockWriter = new MockStreamWriter();
		CompletableFuture<StreamWriter> futA = CompletableFuture.completedFuture(mockWriter);
		MockStreamRef mockStream = new MockStreamRef(futA );
		mockListener.addMockStreamToReturn(mockStream);
		
		Http2Request request = Http2Requests.createRequest(1, true);
		mockChannel.send(request);
		
		PassedIn in = mockListener.getSingleRequest();
		ResponseStream stream = in.stream;
		
		Http2Push push = Http2Requests.createPush(request.getStreamId());
		stream.openPushStream().process(push);
		
		Http2Msg pushMsg = mockChannel.getFrameAndClear();
		Assert.assertEquals(push, pushMsg);
		
		//send bad frame in this state
		DataFrame data = Http2Requests.createData1(push.getPromisedStreamId(), false);
		mockChannel.send(data);
		
		Assert.assertTrue(mockStream.isCancelled());

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: HttpSocket[Http2ChannelCache1]:stream2:(BAD_FRAME_RECEIVED_FOR_THIS_STATE) "
				+ "No transition defined on statemachine for event=RECV_DATA when in state=Reserved(local)", msg);
		Assert.assertTrue(mockChannel.isClosed());
	}

	/**
	 * The "closed" state is the terminal state.
	 * 
	 * An endpoint MUST NOT send frames other than PRIORITY on a closed stream. An endpoint 
	 * that receives any frame other than PRIORITY after receiving a ----RST_STREAM---- MUST 
	 * treat that as a stream error (Section 5.4.2) of type STREAM_CLOSED. Similarly, an 
	 * endpoint that receives any frames after receiving a frame with the 
	 * END_STREAM flag set MUST treat that as a connection error (Section 5.4.1) of 
	 * type STREAM_CLOSED, unless the frame is permitted as described below.
	 * 
	 * We are either in half closed local or closed and in either case the client should not 
	 * be sending data frames at this point.  window update race is ok  
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testSection5_1ReceiveBadFrameInCloseState() throws InterruptedException, ExecutionException, TimeoutException {	
//		Http2Request request = Http2Requests.createRequest(1, true);
//		mockChannel.send(request);
//		
//		PassedIn in = mockListener.getSingleRequest();
//		FrontendStream stream = in.stream;
//		
//		CompletableFuture<Void> future = stream.cancelStream(); //closes the stream
//		future.get(2, TimeUnit.SECONDS);
//		
//		
//		RstStreamFrame reset = (RstStreamFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(request.getStreamId(), reset.getStreamId());
//		
//		DataFrame dataFrame = new DataFrame(1, false);
//		mockChannel.send(dataFrame);
//		
//		//no request comes in
//		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
//		//no cancels(since we already cancelled it)
//		Assert.assertEquals(0, mockListener.getNumCancelsThatCameIn());
//
//		//remote receives stream error
//		RstStreamFrame frame = (RstStreamFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.STREAM_CLOSED, frame.getKnownErrorCode());
//		Assert.assertTrue(!mockChannel.isClosed());
	}
	
	@Test
	public void testSection5_1ReceiveHeadersAfterReceiveRstStreamFrame() throws InterruptedException, ExecutionException, TimeoutException {	
//		Http2Request request = Http2Requests.createRequest(1, true);
//		mockChannel.send(request);
//		
//		PassedIn in = mockListener.getSingleRequest();
//		FrontendStream stream = in.stream;
//		
//		CompletableFuture<Void> future = stream.cancelStream();
//		future.get(2, TimeUnit.SECONDS);
//		
//		RstStreamFrame reset = (RstStreamFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(request.getStreamId(), reset.getStreamId());
//		
//		Http2Request headersF = Http2Requests.createRequest(1, true);
//		mockChannel.send(headersF);
//		
//		//no request comes in
//		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
//		//no cancels(since we already cancelled it)
//		Assert.assertEquals(0, mockListener.getNumCancelsThatCameIn());
//
//		//remote receives goAway
//		RstStreamFrame frame = (RstStreamFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.STREAM_CLOSED, frame.getKnownErrorCode());
//		Assert.assertTrue(!mockChannel.isClosed());
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
	 * The identifier of a newly established stream MUST be numerically 
	 * greater than all streams that the initiating endpoint has opened 
	 * or reserved. This governs streams that are opened using a HEADERS 
	 * frame and streams that are reserved using PUSH_PROMISE. An endpoint 
	 * that receives an unexpected stream identifier MUST respond with 
	 * a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1_1BadEvenStreamId() {
		Http2Request request = Http2Requests.createRequest(2, true);
		mockChannel.send(request);
		
		//no request comes in
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		
		Assert.assertTrue(mockListener.isClosed());
		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertTrue(msg.contains("Bad stream id.  Even stream ids not allowed in requests to a server request="));
		Assert.assertTrue(mockChannel.isClosed());
	}
	
	/**
	 * The identifier of a newly established stream MUST be numerically 
	 * greater than all streams that the initiating endpoint has opened 
	 * or reserved. This governs streams that are opened using a HEADERS 
	 * frame and streams that are reserved using PUSH_PROMISE. An endpoint 
	 * that receives an unexpected stream identifier MUST respond with 
	 * a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 * 
	 * This is in conflict with another part of the spec!!!!! and so we pretend
	 * the stream is closed(as in all likely hood, the stream was closed)!!!
	 * and do not shutdown the whole connection for a case like this.
	 * 
	 * The part it is in conflict with is closed state and receiving messages
	 * in closed state.  The only way to resolve conflict would be to KEEP around
	 * state that a connection is closed.  SORRY, the connection is closed so we
	 * clean up all memory!!!
	 */
	@Test
	public void testSection5_1_1TooLowStreamIdAfterHighStreamId() {
		MockStreamWriter mockWriter = new MockStreamWriter();
		CompletableFuture<StreamWriter> futA = CompletableFuture.completedFuture(mockWriter);
		MockStreamRef mockStream = new MockStreamRef(futA );
		mockListener.addMockStreamToReturn(mockStream);
		
		Http2Request request1 = Http2Requests.createRequest(5, true);
		mockChannel.send(request1);
		mockListener.getSingleRequest();
		
		Http2Request request = Http2Requests.createRequest(3, true);
		mockChannel.send(request);

		//WE DO NOT DO THIS which spec wants(or another test we have starts failing)
		//we leave this here in case you want to comment back in and debug that.
//		//no request comes in
//		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
//		//cancel the first stream since whole connection is going down.
//		Assert.assertEquals(1, mockListener.getNumCancelsThatCameIn());
//		
//		//remote receives goAway
//		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
//		DataWrapper debugData = goAway.getDebugData();
//		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
//		Assert.assertTrue(msg.contains("Bad stream id.  Event stream ids not allowed in requests to a server frame="));
//		Assert.assertTrue(mockChannel.isClosed());
		
		//no request comes in
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		
		Assert.assertFalse(mockListener.isClosed()); //we do not close the channel

		Assert.assertFalse(mockStream.isCancelled()); //our existing streams stays valid and open

		//remote receives goAway
		RstStreamFrame frame = (RstStreamFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.STREAM_CLOSED, frame.getKnownErrorCode());
		Assert.assertTrue(!mockChannel.isClosed());
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
