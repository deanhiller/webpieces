package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.mock.MockChanMgr;
import org.webpieces.http2client.mock.MockHttp2Channel;
import org.webpieces.http2client.mock.MockServerListener;

import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#SETTINGS
 */
public class Test6_5SettingsFrameErrors {

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
	}
	
	@Test
	public void testSection6_5AckNonEmptyPayload() {
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(1L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.getFrameAndClear(); //clear the ack frame 
		
	    String badAckFrame =
	            "00 00 01" + // length
	            "04" +  // type
	            "01" + // flags (ack)
	            "00 00 00 00" + // R + streamid
	            "00"; //payload 
		mockChannel.writeHexBack(badAckFrame); //ack client frame

		//remote receives goAway
		GoAwayFrame goAway = (GoAwayFrame) mockChannel.getFrameAndClear();
		
		//local is notified...
		Assert.assertEquals(ParseFailReason.FRAME_SIZE_INCORRECT_CONNECTION, mockSvrListener.getClosedReason().getReason());
		Assert.assertTrue(mockChannel.isClosed());
	}

	@Test
	public void testSection6_5SettingsStreamIdNonZeroValue() {
		
	}
	
	@Test
	public void testSection6_5SettingsFrameLengthMultipleNotSixOctects() {
	}	
	
	@Test
	public void testSection6_5_2PushPromiseOffButServerSentIt() {
	}
	
	@Test
	public void testSection6_5_2InitialWindowSizeTooLarge() {
	}
	
	@Test
	public void testSection6_5_2MaxFrameSizeOutsideAllowedRange() {
	}
	
	@Test
	public void testSection6_5_3SettingsAckNotReceivedInReasonableTime() {
	}
}
