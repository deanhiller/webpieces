package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.mock.MockChanMgr;
import org.webpieces.http2client.mock.MockHttp2Channel;
import org.webpieces.http2client.mock.MockPushListener;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.util.threading.DirectExecutor;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2engine.api.client.ClientStreamWriter;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class Test5_1StreamStates {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private MockChanMgr mockChanMgr;
	private MockHttp2Channel mockChannel;
	private Http2Socket httpSocket;
	private HeaderSettings localSettings = Requests.createSomeSettings();

	@Before
	public void setUp() throws InterruptedException, ExecutionException {
		
        mockChanMgr = new MockChanMgr();
        mockChannel = new MockHttp2Channel();
        mockChannel.setIncomingFrameDefaultReturnValue(CompletableFuture.completedFuture(mockChannel));

        Http2Config config = new Http2Config();
        config.setInitialRemoteMaxConcurrent(1); //start with 1 max concurrent
        localSettings.setInitialWindowSize(localSettings.getMaxFrameSize()*4);
        config.setLocalSettings(localSettings);
        Http2Client client = Http2ClientFactory.createHttpClient(config, mockChanMgr, new DirectExecutor());
        
        mockChanMgr.addTCPChannelToReturn(mockChannel);
		httpSocket = client.createHttpSocket("simple");
		
		CompletableFuture<Http2Socket> connect = httpSocket.connect(new InetSocketAddress(555));
		Assert.assertTrue(connect.isDone());
		Assert.assertEquals(httpSocket, connect.get());

		//clear preface and settings frame from client
		mockChannel.getFramesAndClear();
		
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(1L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.getFrameAndClear(); //clear the ack frame 
	}
	
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
		Assert.assertEquals("Stream in idle state and received this frame which should not happen in idle state.  "
				+ "frame=DataFrame{streamId=1, endStream=false, data.len=0, padding=0}  reason=BAD_FRAME_RECEIVED_FOR_THIS_STATE stream=1", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		//send new request on closed connection
		MockResponseListener listener1 = new MockResponseListener();
		Http2Headers request1 = Requests.createRequest();
		CompletableFuture<ClientStreamWriter> future = httpSocket.send(request1, listener1);
		
		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
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
		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
		listener1.addReturnValuePush(pushListener);
		
		Http2Headers request = sendRequestToServer(listener1);
		Http2Push svrPush = sendPushFromServer(listener1, request);
		
		Http2Push push = (Http2Push) pushListener.getSingleParam();
		Assert.assertEquals(svrPush, push);
		
		DataFrame dataFrame = new DataFrame(push.getPromisedStreamId(), false);
		mockChannel.write(dataFrame);

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("No transition defined on statemachine for event=Http2Event "
				+ "[sendReceive=RECEIVE, payloadType=DATA] when in state=Reserved(remote) "
				+ "reason=BAD_FRAME_RECEIVED_FOR_THIS_STATE stream=2", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		ConnectionReset failResp1 = (ConnectionReset) listener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(ParseFailReason.BAD_FRAME_RECEIVED_FOR_THIS_STATE, failResp1.getReason().getReason());
		
		ConnectionReset failResp2 = (ConnectionReset) pushListener.getSingleParam();
		Assert.assertEquals(ParseFailReason.BAD_FRAME_RECEIVED_FOR_THIS_STATE, failResp2.getReason().getReason());
	}

	/**
	 * Similarly, an endpoint that receives any frames after receiving a frame 
	 * with the END_STREAM flag set MUST treat that as a connection error (Section 5.4.1) of
	 * type STREAM_CLOSED, unless the frame is permitted as described below.
	 */
	@Test
	public void testSection5_1ReceiveBadFrameAfterEndStream() {	
		
	}

	/**
	 * Similarly, an endpoint that receives any frames after receiving a frame 
	 * with the END_STREAM flag set MUST treat that as a connection error (Section 5.4.1) of
	 * type STREAM_CLOSED, unless the frame is permitted as described below.
	 *  
	 *  WINDOW_UPDATE or RST_STREAM frames can be received in this state for 
	 *  a short period after a DATA or HEADERS frame containing an END_STREAM flag 
	 *  is sent. Until the remote peer receives and processes RST_STREAM or the 
	 *  frame bearing the END_STREAM flag, it might send frames of these types. Endpoints 
	 *  MUST ignore WINDOW_UPDATE or RST_STREAM frames received in this state, though 
	 *  endpoints MAY choose to treat frames that arrive a significant time after 
	 *  sending END_STREAM as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
	 */
	@Test
	public void testSection5_1ReceiveGoodFrameAfterEndStream() {	
		
	}
	
	/**
	 * An endpoint that receives any frame other than PRIORITY after receiving a 
	 * RST_STREAM MUST treat that as a stream error (Section 5.4.2) of type STREAM_CLOSED.
	 */
	@Test
	public void testSection5_1ReceiveBadFrameAfterRstFrame() {	
		
	}
	
	/**
	 * An endpoint that receives any frame other than PRIORITY after receiving a 
	 * RST_STREAM MUST treat that as a stream error (Section 5.4.2) of type STREAM_CLOSED.
	 */
	@Test
	public void testSection5_1ReceivePriorityFrameAfterRstFrame() {	
	}
	
	private Http2Push sendPushFromServer(MockResponseListener listener1, Http2Headers request) {
		Http2Push resp1 = Requests.createPush(request.getStreamId());
		mockChannel.write(resp1); 
		int response1 = listener1.getSinglePushStreamId();
		Assert.assertEquals(resp1.getPromisedStreamId(), response1);
		return resp1;
	}
	
//	private void sendResponseFromServer(MockResponseListener listener1, Http2Headers request) {
//		Http2Headers resp1 = Requests.createResponse(request.getStreamId());
//		mockChannel.write(resp1); //endOfStream=false
//		PartialStream response1 = listener1.getSingleReturnValueIncomingResponse();
//		Assert.assertEquals(resp1, response1);
//	}

	private Http2Headers sendRequestToServer(MockResponseListener listener1) {
		Http2Headers request1 = Requests.createRequest();

		httpSocket.send(request1, listener1);
		
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(request1, req);
		return request1;
	}

}
