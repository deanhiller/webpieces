package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
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
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockServerListener;

import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.impl.HeaderEncoding;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.ErrorType;
import com.webpieces.http2parser.api.Http2Exception;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class Test4FrameSizeAndHeaders {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private MockChanMgr mockChanMgr;
	private MockHttp2Channel mockChannel;
	private Http2Socket socket;
	private HeaderSettings localSettings = Requests.createSomeSettings();
	private MockServerListener mockSvrListener;

	@Before
	public void setUp() throws InterruptedException, ExecutionException {
		
        mockChanMgr = new MockChanMgr();
        mockChannel = new MockHttp2Channel();
        mockChannel.setIncomingFrameDefaultReturnValue(CompletableFuture.completedFuture(mockChannel));

        Http2Config config = new Http2Config();
        config.setInitialRemoteMaxConcurrent(1); //start with 1 max concurrent
        localSettings.setInitialWindowSize(localSettings.getMaxFrameSize()*4);
        config.setLocalSettings(localSettings);
        Http2Client client = Http2ClientFactory.createHttpClient(config, mockChanMgr);
        
        mockChanMgr.addTCPChannelToReturn(mockChannel);
		socket = client.createHttpSocket("simple");
		
		mockSvrListener = new MockServerListener();
		CompletableFuture<Http2Socket> connect = socket.connect(new InetSocketAddress(555), mockSvrListener);
		Assert.assertTrue(connect.isDone());
		Assert.assertEquals(socket, connect.get());

		//clear preface and settings frame from client
		mockChannel.getFramesAndClear();
		
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(1L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.getFrameAndClear(); //clear the ack frame 
	}
	
	@Test
	public void testSection4_2FrameTooLarge() {		
		MockResponseListener listener1 = new MockResponseListener();
		Http2Headers request = sendRequestToServer(listener1);
		sendResponseFromServer(listener1, request);
		
		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		byte[] buf = new byte[localSettings.getMaxFrameSize()+4];
		dataFrame.setData(dataGen.wrapByteArray(buf));
		mockChannel.write(dataFrame); //endOfStream=false

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("Frame size=16389 was greater than max="+localSettings.getMaxFrameSize()+" reason=EXCEEDED_MAX_FRAME_SIZE stream=1", msg);
		
		//local is notified...
		Http2Exception reason = mockSvrListener.getClosedReason();
		Assert.assertEquals(Http2ErrorCode.FRAME_SIZE_ERROR, reason.getReason().getErrorCode());
		Assert.assertEquals(ErrorType.CONNECTION, reason.getErrorType());
		Assert.assertTrue(mockChannel.isClosed());
	}

	
	@Test
	public void testSection4_2LargestFrame() {		
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
		Http2Headers request = sendRequestToServer(listener1);
		sendResponseFromServer(listener1, request);
		
		DataFrame dataFrame = new DataFrame(request.getStreamId(), false);
		byte[] buf = new byte[localSettings.getMaxFrameSize()];
		dataFrame.setData(dataGen.wrapByteArray(buf));
		mockChannel.write(dataFrame); //endOfStream=false

		//remote receives goAway
		DataFrame fr = (DataFrame) listener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(localSettings.getMaxFrameSize(), fr.getData().getReadableSize());
	}
	
	private void sendResponseFromServer(MockResponseListener listener1, Http2Headers request) {
		Http2Headers resp1 = Requests.createResponse(request.getStreamId());
		mockChannel.write(resp1); //endOfStream=false
		PartialStream response1 = listener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(resp1, response1);
	}

	private Http2Headers sendRequestToServer(MockResponseListener listener1) {
		Http2Headers request1 = Requests.createRequest();

		socket.sendRequest(request1, listener1);
		
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(request1, req);
		return request1;
	}

	@Test
	public void testSection4_3BadDecompression() {		
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
		Http2Headers request = sendRequestToServer(listener1);
		
		Assert.assertEquals(1, request.getStreamId()); //has to be 1 since we use 1 in the response
		
	    String badHeaderFrame =
	            "00 00 10" + // length
	            "01" +  // type
	            "05" + // flags (ack)
	            "00 00 00 01" + // R + streamid
	            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"; //payload 
		mockChannel.writeHexBack(badHeaderFrame); //endOfStream=false

		mockSvrListener.assertNotClosedd();
		Assert.assertFalse(mockChannel.isClosed());
		RstStreamFrame reset = (RstStreamFrame) listener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(Http2ErrorCode.COMPRESSION_ERROR, reset.getKnownErrorCode());
	}
	
	@Test
	public void testSection4_3InterleavedFrames() {
		MockResponseListener listener1 = new MockResponseListener();
		listener1.setIncomingRespDefault(CompletableFuture.<Void>completedFuture(null));
		Http2Headers request = sendRequestToServer(listener1);
		
		Assert.assertEquals(1, request.getStreamId()); //has to be 1 since we use 1 in the response
	
		List<Http2Frame> frames = createInterleavedFrames();
		Assert.assertTrue(frames.size() >= 3); //for this test, need interleaved
		
		mockChannel.writeFrame(frames.get(0));
		Assert.assertEquals(0, listener1.getReturnValuesIncomingResponse().size());

		mockChannel.writeFrame(frames.get(1));
		Http2Exception reason = mockSvrListener.getClosedReason();
		ParseFailReason failReason = reason.getReason();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, failReason.getErrorCode());
		Assert.assertEquals(ErrorType.CONNECTION, reason.getErrorType());

		//Assert.assertEquals(1, listener1.getReturnValuesIncomingResponse().size());

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
