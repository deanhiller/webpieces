package org.webpieces.http2client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockStreamWriter;
import org.webpieces.http2client.mock.TestAssert;
import org.webpieces.http2client.util.Requests;

import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.impl.HeaderEncoding;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.error.ConnectionClosedException;
import com.webpieces.http2engine.api.error.ShutdownStream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class TestC4FrameSizeAndHeaders extends AbstractTest {
	
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
		MockStreamWriter mockStreamWriter = new MockStreamWriter();
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(mockStreamWriter));
		Http2Request request = sendRequestToServer(listener1);
		sendResponseFromServer(listener1, request);
		
		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		byte[] buf = new byte[localSettings.getMaxFrameSize()+4];
		dataFrame.setData(DATA_GEN.wrapByteArray(buf));
		mockChannel.write(dataFrame); //endOfStream=false

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.FRAME_SIZE_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: stream1:(EXCEEDED_MAX_FRAME_SIZE) Frame size=16389 was greater than max=16385", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		ShutdownStream failResp = (ShutdownStream) listener1.getSingleRstStream();
		Assert.assertEquals(CancelReasonCode.EXCEEDED_MAX_FRAME_SIZE, failResp.getCause().getReasonCode());

		//send new request on closed connection
		Http2Request request1 = Requests.createRequest();
		CompletableFuture<StreamWriter> future = httpSocket.openStream().process(request1, listener1);
		
		ConnectionClosedException intercept = (ConnectionClosedException) TestAssert.intercept(future);
		Assert.assertTrue(intercept.getMessage().contains("Connection closed or closing"));
		Assert.assertEquals(0, mockChannel.getFramesAndClear().size());
	}

	/**
	 * success case of edge testing off by one or not
	 */
	@Test
	public void testSection4_2CanSendLargestFrame() {
		MockStreamWriter mockStreamWriter = new MockStreamWriter();
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(mockStreamWriter));
		Http2Request request = sendRequestToServer(listener1);
		sendResponseFromServer(listener1, request);
		
		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		byte[] buf = new byte[localSettings.getMaxFrameSize()];
		dataFrame.setData(DATA_GEN.wrapByteArray(buf));
		mockChannel.write(dataFrame); //endOfStream=false

		DataFrame fr = (DataFrame) mockStreamWriter.getSingleFrame();
		Assert.assertEquals(localSettings.getMaxFrameSize(), fr.getData().getReadableSize());
	}

	/**
	 *  A decoding error in a header block MUST be treated as a connection error (Section 5.4.1) of type COMPRESSION_ERROR.
	 *  
	 */
	@Test
	public void testSection4_3BadDecompression() {		
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(null));
		Http2Request request = sendRequestToServer(listener1);
		
		Assert.assertEquals(1, request.getStreamId()); //has to be 1 since we use 1 in the response
		
	    String badHeaderFrame =
	            "00 00 10" + // length
	            "01" +  // type
	            "05" + // flags (ack)
	            "00 00 00 01" + // R + streamid
	            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"; //payload 
		mockChannel.writeHexBack(badHeaderFrame); //endOfStream=false

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.COMPRESSION_ERROR, goAway.getKnownErrorCode());
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("ConnectionException: MockHttp2Channel1:stream1:(HEADER_DECODE) Error from hpack library", msg);
		Assert.assertTrue(mockChannel.isClosed());
		
		List<CancelReason> results = listener1.getRstStreams();
		Assert.assertEquals(1, results.size());
		ShutdownStream failResp = (ShutdownStream) results.get(0);
		Assert.assertEquals(CancelReasonCode.HEADER_DECODE, failResp.getCause().getReasonCode());
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
		MockStreamWriter mockStreamWriter = new MockStreamWriter();
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<StreamWriter>completedFuture(mockStreamWriter));
		Http2Request request = sendRequestToServer(listener1);
		
		Assert.assertEquals(1, request.getStreamId()); //has to be 1 since we use 1 in the response
	
		List<Http2Frame> frames = createInterleavedFrames();
		Assert.assertTrue(frames.size() >= 3); //for this test, need interleaved
		
		mockChannel.writeFrame(frames.get(0));
		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());

		mockChannel.writeFrame(frames.get(1));
		ShutdownStream reset = (ShutdownStream) listener1.getSingleRstStream();
		Assert.assertEquals(CancelReasonCode.HEADERS_MIXED_WITH_FRAMES, reset.getCause().getReasonCode());
		
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
