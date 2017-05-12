package org.webpieces.httpfrontend2.api.http2;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.httpfrontend2.api.mock2.MockRequestListener.Cancel;
import org.webpieces.httpfrontend2.api.mock2.MockRequestListener.PassedIn;

import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.impl.HeaderEncoding;
import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class Test4FrameSizeAndHeaders extends AbstractHttp2Test {
	
	/**
	 * An endpoint MUST send an error code of FRAME_SIZE_ERROR if a frame 
	 * exceeds the size defined in SETTINGS_MAX_FRAME_SIZE, exceeds any 
	 * limit defined for the frame type, or is too small to contain 
	 * mandatory frame data. A frame size error in a frame that could alter 
	 * the state of the entire connection MUST be treated as a connection 
	 * error (Section 5.4.1); this includes any frame carrying a header 
	 * block (Section 4.3) (that is, HEADERS, PUSH_PROMISE, and 
	 * CONTINUATION), SETTINGS, and any frame with a stream identifier of 0.
	 */
	@Test
	public void testSection4_2FrameTooLarge() {
		int streamId = 1;
		PassedIn info = sendRequestToServer(streamId, false);
		FrontendStream stream = info.stream;
		Http2Headers request = info.request;

		//send data that goes with request
		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		byte[] buf = new byte[localSettings.getMaxFrameSize()+4];
		dataFrame.setData(dataGen.wrapByteArray(buf));
		mockChannel.write(dataFrame); //endOfStream=false

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.FRAME_SIZE_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("Frame size=16389 was greater than max="+localSettings.getMaxFrameSize()+" reason=EXCEEDED_MAX_FRAME_SIZE stream=1", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		Cancel failResp = mockListener.getCancelInfo();
		ConnectionReset reset = (ConnectionReset) failResp.reset;
		Assert.assertEquals(ParseFailReason.EXCEEDED_MAX_FRAME_SIZE, reset.getReason().getReason());

		//send response with request not complete but failed as well anyways
		Http2Headers response = Http2Requests.createResponse();
		stream.sendResponse(response);
		
//		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
//		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
//		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
	}

	/**
	 * success case of edge testing off by one or not
	 */
	@Test
	public void testSection4_2CanSendLargestFrame() {
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		Http2Headers request = sendRequestToServer(listener1);
//		sendResponseFromServer(listener1, request);
//		
//		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
//		byte[] buf = new byte[localSettings.getMaxFrameSize()];
//		dataFrame.setData(dataGen.wrapByteArray(buf));
//		mockChannel.write(dataFrame); //endOfStream=false
//
//		DataFrame fr = (DataFrame) listener1.getSingleReturnValueIncomingResponse();
//		Assert.assertEquals(localSettings.getMaxFrameSize(), fr.getData().getReadableSize());
	}

	/**
	 *  A decoding error in a header block MUST be treated as a connection error (Section 5.4.1) of type COMPRESSION_ERROR.
	 *  
	 */
	@Test
	public void testSection4_3BadDecompression() {		
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		Http2Headers request = sendRequestToServer(listener1);
//		
//		Assert.assertEquals(1, request.getStreamId()); //has to be 1 since we use 1 in the response
//		
//	    String badHeaderFrame =
//	            "00 00 10" + // length
//	            "01" +  // type
//	            "05" + // flags (ack)
//	            "00 00 00 01" + // R + streamid
//	            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"; //payload 
//		mockChannel.writeHexBack(badHeaderFrame); //endOfStream=false
//
//		//remote receives goAway
//		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.COMPRESSION_ERROR, goAway.getKnownErrorCode());
//		DataWrapper debugData = goAway.getDebugData();
//		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
//		Assert.assertEquals("Error from hpack library reason=HEADER_DECODE stream=1", msg);
//		Assert.assertTrue(mockChannel.isClosed());
//		
//		List<PartialStream> results = listener1.getReturnValuesIncomingResponse();
//		Assert.assertEquals(1, results.size());
//		ConnectionReset failResp = (ConnectionReset) results.get(0);
//		Assert.assertEquals(ParseFailReason.HEADER_DECODE, failResp.getReason().getReason());
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
//		MockResponseListener listener1 = new MockResponseListener();
//		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
//		Http2Headers request = sendRequestToServer(listener1);
//		
//		Assert.assertEquals(1, request.getStreamId()); //has to be 1 since we use 1 in the response
//	
//		List<Http2Frame> frames = createInterleavedFrames();
//		Assert.assertTrue(frames.size() >= 3); //for this test, need interleaved
//		
//		mockChannel.writeFrame(frames.get(0));
//		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());
//
//		mockChannel.writeFrame(frames.get(1));
//		List<PartialStream> results = listener1.getReturnValuesIncomingResponse();
//		Assert.assertEquals(1, results.size());
//		ConnectionReset reset = (ConnectionReset) results.get(0);
//		Assert.assertEquals(ParseFailReason.HEADERS_MIXED_WITH_FRAMES, reset.getReason().getReason());
//		
//		//remote receives goAway
//		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
//		DataWrapper debugData = goAway.getDebugData();
//		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
//		Assert.assertTrue(msg.contains("Headers/continuations from two different streams per spec cannot be interleaved. "));
//		Assert.assertTrue(mockChannel.isClosed());
	}

	private List<Http2Frame> createInterleavedFrames() {
		Http2Headers response1 = new Http2Headers();
		response1.setStreamId(1);
		response1.setEndOfStream(true);
		fillHeaders(response1);
		
		HeaderEncoding encoding = new HeaderEncoding();
		List<Http2Frame> frames1 = encoding.translateToFrames(localSettings.getMaxFrameSize(), new Encoder(localSettings.getHeaderTableSize()), response1);
		
		Http2Headers response2 = new Http2Headers();
		response2.setStreamId(3);
		response1.setEndOfStream(true);
		response2.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "value"));
		List<Http2Frame> frames2 = encoding.translateToFrames(localSettings.getMaxFrameSize(), new Encoder(localSettings.getHeaderTableSize()), response2);

		List<Http2Frame> frames = new ArrayList<>();
		frames.addAll(frames1);
		frames.add(1, frames2.get(0));
		return frames;
	}

	private void fillHeaders(Http2Headers response1) {
		String value = "heaheaheaheaheaheahahoz.zhxheh,h,he,he,heaheaeaheaheahoahoahozzoqorqzro.zo.zrszaroatroathoathoathoathoatoh";
		for(int i = 0; i < 10; i++) {
			value = value + value;
			response1.addHeader(new Http2Header("eaheahaheaheaeha"+i, value));
		}
	}
}
