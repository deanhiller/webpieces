package org.webpieces.httpfrontend2.api.http2;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class TestS6_5SettingsFrameErrors extends AbstractHttp2Test {
	
	@Override
	protected void simulateClientSendingPrefaceAndSettings() {
		//null out the settings stuff
		mockChannel.sendPreface();
		SettingsFrame settings = (SettingsFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(0, settings.getStreamId());
	}
	
	@Ignore
	@Test
	public void testSection6_5_3SettingsAckNotReceivedInReasonableTime() {
	}
	
	@Test
	public void testSection6_5AckNonEmptyPayload() {		
		
	    String badAckFrame =
	            "00 00 01" + // length
	            "04" +  // type
	            "01" + // flags (ack)
	            "00 00 00 00" + // R + streamid
	            "00"; //payload 
		mockChannel.sendHexBack(badAckFrame); //ack client frame

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		DataWrapper debugData = goAway.getDebugData();
		String msg = debugData.createStringFromUtf8(0, debugData.getReadableSize());
		Assert.assertEquals("size of payload of a settings frame ack must be 0 but was=1 reason=FRAME_SIZE_INCORRECT stream=0", msg);
		Assert.assertTrue(mockChannel.isClosed());
	}

	@Test
	public void testSection6_5SettingsStreamIdNonZeroValue() {
		//server's settings frame is finally coming in as well with maxConcurrent=1
	    String badStreamIdSettings =
	            "00 00 0C" + // length
	            "04" +  // type
	            "00" + //flags
	            "00 00 00 01" + //R + streamid
	            "00 02 00 00 00 01" + //setting 1 (enable push)
	            "00 03 00 00 01 00"; //setting 2 (max streams)
	    
		mockChannel.sendHexBack(badStreamIdSettings);
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());
		Assert.assertTrue(mockChannel.isClosed());
	}
	
	@Test
	public void testSection6_5SettingsFrameLengthMultipleNotSixOctects() {
		//server's settings frame is finally coming in as well with maxConcurrent=1
	    String badStreamIdSettings =
	            "00 00 0B" + // length
	            "04" +  // type
	            "00" + //flags
	            "00 00 00 00" + //R + streamid
	            "00 02 00 00 00 01" + //setting 1 (enable push)
	            "00 03 00 00 01"; //setting 2 (max streams)
	    
		mockChannel.sendHexBack(badStreamIdSettings);
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.FRAME_SIZE_ERROR, goAway.getKnownErrorCode());

		Assert.assertTrue(mockChannel.isClosed());
	}
	
	@Test
	public void testSection6_5_2InitialWindowSizeTooLarge() {
		//server's settings frame is finally coming in as well with maxConcurrent=1
	    String badStreamIdSettings =
	            "00 00 0C" + // length
	            "04" +  // type
	            "00" + //flags
	            "00 00 00 00" + //R + streamid
	            "00 02 00 00 00 01" + //setting 1 (enable push)
	            "00 04 FF FF FF FF"; //setting 2 (initial window size)
	    
		mockChannel.sendHexBack(badStreamIdSettings);
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.FLOW_CONTROL_ERROR, goAway.getKnownErrorCode());

		Assert.assertTrue(mockChannel.isClosed());
	}
	
	@Test
	public void testSection6_5_2MaxFrameSizeOutsideAllowedRange() {
		//server's settings frame is finally coming in as well with maxConcurrent=1
	    String badStreamIdSettings =
	            "00 00 0C" + // length
	            "04" +  // type
	            "00" + //flags
	            "00 00 00 00" + //R + streamid
	            "00 02 00 00 00 01" + //setting 1 (enable push)
	            "00 05 00 00 00 00"; //setting 2 (initial window size)
	    
		mockChannel.sendHexBack(badStreamIdSettings);
		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(Http2ErrorCode.PROTOCOL_ERROR, goAway.getKnownErrorCode());

		Assert.assertTrue(mockChannel.isClosed());
	}
	
}
