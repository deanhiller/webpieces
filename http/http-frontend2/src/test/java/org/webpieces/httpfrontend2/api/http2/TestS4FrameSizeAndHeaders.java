package org.webpieces.httpfrontend2.api.http2;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpfrontend2.api.mock2.MockStreamRef;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;
import org.webpieces.httpfrontend2.api.mock2.TestAssert;

import com.twitter.hpack.Encoder;
import com.webpieces.hpack.impl.HeaderEncoding;
import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.GoAwayFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2ErrorCode;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.api.error.ConnectionClosedException;
import com.webpieces.http2engine.api.error.ShutdownStream;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class TestS4FrameSizeAndHeaders extends AbstractFrontendHttp2Test {
	
	/**
	 * An endpoint MUST send an error code of FRAME_SIZE_ERROR if a frame 
	 * exceeds the size defined in SETTINGS_MAX_FRAME_SIZE, exceeds any 
	 * limit defined for the frame type, or is too small to contain 
	 * mandatory frame data. A frame size error in a frame that could alter 
	 * the state of the entire connection MUST be treated as a connection 
	 * error (Section 5.4.1); this includes any frame carrying a header 
	 * block (Section 4.3) (that is, HEADERS, PUSH_PROMISE, and 
	 * CONTINUATION), SETTINGS, and any frame with a stream identifier of 0.
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testSection4_2FrameTooLarge() throws InterruptedException, ExecutionException, TimeoutException {
		MockStreamWriter mockWriter = new MockStreamWriter();
		XFuture<StreamWriter> futA = XFuture.completedFuture(mockWriter);
		MockStreamRef mockStream = new MockStreamRef(futA );
		mockListener.addMockStreamToReturn(mockStream);
		
		int streamId = 1;
		PassedIn info = sendRequestToServer(streamId, false);
		ResponseStream stream = info.stream;
		Http2Request request = info.request;

		Assert.assertFalse(mockStream.isCancelled());

		//send data that goes with request
		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		byte[] buf = new byte[localSettings.getMaxFrameSize()+4];
		dataFrame.setData(DATA_GEN.wrapByteArray(buf));
		mockChannel.send(dataFrame); //endOfStream=false

		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.FRAME_SIZE_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: stream1:(EXCEEDED_MAX_FRAME_SIZE) Frame size=16389 was greater than max=16385", msg);
		Assert.assertTrue(mockChannel.isClosed());

		Assert.assertTrue(mockListener.isClosed());

		Assert.assertTrue(mockStream.isCancelled());
		CancelReason failResp = mockStream.getCancelInfo();

		ShutdownStream reset = (ShutdownStream) failResp;
		Assert.assertEquals(CancelReasonCode.EXCEEDED_MAX_FRAME_SIZE, reset.getCause().getReasonCode());

		//send response with request not complete but failed as well anyways
		Http2Response response = Http2Requests.createResponse(request.getStreamId());
		XFuture<StreamWriter> future = stream.process(response);
		
		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
	}

	/**
	 *  A decoding error in a header block MUST be treated as a connection error (Section 5.4.1) of type COMPRESSION_ERROR.
	 *  
	 */
	@Test
	public void testSection4_3BadDecompression() {		
	    String badHeaderFrame =
	            "00 00 10" + // length
	            "01" +  // type
	            "05" + // flags (ack)
	            "00 00 00 01" + // R + streamid
	            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"; //payload 
		mockChannel.sendHexBack(badHeaderFrame); //endOfStream=false

		//no request comes in
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());

		Assert.assertTrue(mockListener.isClosed());
		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.COMPRESSION_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: HttpSocket[Http2ChannelCache1]:stream1:(HEADER_DECODE) Error from hpack library", msg);
		Assert.assertTrue(mockChannel.isClosed());
	}
	

	/**
	 *  A decoding error in a header block MUST be treated as a connection error (Section 5.4.1) of type COMPRESSION_ERROR.
	 *  
	 */
	@Test
	public void testBadSizePriorityFrame() {
	    String priorityFrame = 
	        	"00 00 06" + //length
	            "02" + //type
	            "00" + //flags
	            "00 00 00 01" + // R + streamid
	            "80 00 00 04" + // stream dependency
	            "05 00"; // weight
		mockChannel.sendHexBack(priorityFrame); //endOfStream=false

		//no request comes in
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		
		Assert.assertTrue(mockListener.isClosed());
		
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.FRAME_SIZE_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: stream1:(FRAME_SIZE_INCORRECT) priority size not 5 and instead is=6", msg);
		Assert.assertTrue(mockChannel.isClosed());
	}
	
	/**
	 * Each header block is processed as a discrete unit. Header blocks 
	 * MUST be transmitted as a contiguous sequence of frames, with no interleaved 
	 * frames of any other type or from any other stream. The last frame in a 
	 * sequence of HEADERS or CONTINUATION frames has the END_HEADERS flag set. The 
	 * last frame in a sequence of PUSH_PROMISE or CONTINUATION frames has the 
	 * END_HEADERS flag set. This allows a header block to be logically equivalent to a single frame.
	 * 
	 * Header block fragments can only be sent as the payload of HEADERS, PUSH_PROMISE, or 
	 * CONTINUATION frames because these frames carry data that can modify the 
	 * compression context maintained by a receiver. An endpoint receiving 
	 * HEADERS, PUSH_PROMISE, or CONTINUATION frames needs to reassemble header 
	 * blocks and perform decompression even if the frames are to be discarded. A receiver 
	 * MUST terminate the connection with a connection error (Section 5.4.1) of 
	 * type COMPRESSION_ERROR if it does not decompress a header block.
	 */
	@Test
	public void testSection4_3InterleavedFrames() {
		List<Http2Frame> frames = createInterleavedFrames();
		Assert.assertTrue(frames.size() >= 3); //for this test, need interleaved

		mockChannel.sendFrame(frames.get(0));
		mockChannel.sendFrame(frames.get(1));
		
		//no request comes in
		Assert.assertEquals(0, mockListener.getNumRequestsThatCameIn());
		
		Assert.assertTrue(mockListener.isClosed());

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertTrue(msg.contains("Headers/continuations from two different streams per spec cannot be interleaved. "));
		Assert.assertTrue(mockChannel.isClosed());

	}

	private List<Http2Frame> createInterleavedFrames() {
		Http2Response response1 = new Http2Response();
		response1.setStreamId(1);
		response1.setEndOfStream(true);
		fillHeaders(response1);
		
		HeaderEncoding encoding = new HeaderEncoding();
		List<Http2Frame> frames1 = encoding.translateToFrames(localSettings.getMaxFrameSize(), new Encoder(localSettings.getHeaderTableSize()), response1);
		
		Http2Response response2 = new Http2Response();
		response2.setStreamId(3);
		response1.setEndOfStream(true);
		response2.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "value"));
		List<Http2Frame> frames2 = encoding.translateToFrames(localSettings.getMaxFrameSize(), new Encoder(localSettings.getHeaderTableSize()), response2);

		List<Http2Frame> frames = new ArrayList<>();
		frames.addAll(frames1);
		frames.add(1, frames2.get(0));
		return frames;
	}

	private void fillHeaders(Http2Response response1) {
		String value = "heaheaheaheaheaheahahoz.zhxheh,h,he,he,heaheaeaheaheahoahoahozzoqorqzro.zo.zrszaroatroathoathoathoathoatoh";
		for(int i = 0; i < 10; i++) {
			value = value + value;
			response1.addHeader(new Http2Header("eaheahaheaheaeha"+i, value));
		}
	}
}
