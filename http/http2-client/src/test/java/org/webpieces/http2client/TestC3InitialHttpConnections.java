package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.mock.MockChanMgr;
import org.webpieces.http2client.mock.MockHttp2Channel;
import org.webpieces.http2client.mock.Preface;
import org.webpieces.http2client.util.Requests;
import org.webpieces.mock.time.MockTime;

import com.webpieces.http2.api.dto.lowlevel.SettingsFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#starting
 */
public class TestC3InitialHttpConnections {

	private MockChanMgr mockChanMgr = new MockChanMgr();
	private MockHttp2Channel mockChannel = new MockHttp2Channel();;
	private Http2Socket socket;
	private HeaderSettings localSettings = Requests.createSomeSettings();
	private MockTime mockTime = new MockTime(true);

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
        mockChannel.setIncomingFrameDefaultReturnValue(XFuture.completedFuture(null));

        Http2Config config = new Http2Config();
        config.setInitialRemoteMaxConcurrent(1); //start with 1 max concurrent
        config.setLocalSettings(localSettings);      
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		InjectionConfig injConfig = new InjectionConfig(mockTime, config, metrics);
        Http2Client client = Http2ClientFactory.createHttpClient("test2Client", mockChanMgr, injConfig);
        
        mockChanMgr.addTCPChannelToReturn(mockChannel);
		socket = client.createHttpSocket(new SocketListener());
		
		XFuture<Void> connect = socket.connect(new InetSocketAddress(555));
		connect.get(2, TimeUnit.SECONDS);
		
	}

	/**
	 * Works with everyone but incurs a round trip overhead
	 * 
	 * should send 
	 * 
	 * GET / HTTP/1.1
	 * Host: server.example.com
	 * Connection: Upgrade, HTTP2-Settings
	 * Upgrade: h2c
	 * HTTP2-Settings: <base64url encoding of HTTP/2 SETTINGS payload>
	 * 
	 * Server sends
	 * HTTP/1.1 101 Switching Protocols
	 * Connection: Upgrade
	 * Upgrade: h2c
	 * 
	 * The first HTTP/2 frame sent by the server MUST be a server connection preface (Section 3.5) 
	 * consisting of a SETTINGS frame (Section 6.5). Upon 
	 * receiving the 101 response, the client MUST send a connection preface
	 *  (Section 3.5), which includes a SETTINGS frame.
	 */
	@Test
	public void testSection3_2WithH2cTokenAfterUpgrade() {
		//not sure we need ever so implement only when we need it
	}
	
	/**
	 * Only will work with webpieces and 'jetty with alpn installed'
	 * 
	 * should send PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n
	 * and server sends back it's preface...
	 * The server connection preface consists of a potentially empty SETTINGS 
	 * frame (Section 6.5) that MUST be the first frame the server sends in the HTTP/2 connection.
	 * 
	 * SettingsFrame{streamId=0, ack=false, settings=[{SETTINGS_HEADER_TABLE_SIZE: 4096}, {SETTINGS_MAX_CONCURRENT_STREAMS: 1024}, {SETTINGS_INITIAL_WINDOW_SIZE: 65535}, {SETTINGS_MAX_HEADER_LIST_SIZE: 8192}]} 
	 * SettingsFrame{streamId=0, ack=true, settings=[]} 
	 */
	@Test
	public void testSection3_4WithH2cTokenPriorKnowledge() {
		//verify settings on connect were sent
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Preface preface = (Preface) frames.get(0);
		preface.verify();
		Http2Msg settings1 = frames.get(1);
		Assert.assertEquals(HeaderSettings.createSettingsFrame(localSettings), settings1);
		
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(1L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.write(new SettingsFrame(true)); //ack client frame
		
		SettingsFrame clientAck = (SettingsFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(true, clientAck.isAck());
	}
	
	@Test
	public void testSection3_4WithH2cTokenFailsIfHttp1_1() {
		//do we want to implement?  basically some weird parse error happens and we could catch 
		//and say this appears not to be http2 to the client
	}
	
	
}
