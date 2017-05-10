package org.webpieces.httpfrontend2.api.http2;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.httpfrontend2.api.mock2.MockChanMgr;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2Channel;
import org.webpieces.httpfrontend2.api.mock2.MockRequestListener;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;
import org.webpieces.httpfrontend2.api.mock2.MockTcpServerChannel;
import org.webpieces.httpfrontend2.api.mock2.MockTimer;
import org.webpieces.mock.time.MockTime;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.threading.DirectExecutor;

import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.HeaderSettings;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#starting
 */
public class Test3InitialHttpConnections {

	private MockChanMgr mockChanMgr = new MockChanMgr();
	private MockHttp2Channel mockChannel = new MockHttp2Channel();
	private HeaderSettings localSettings = Http2Requests.createSomeSettings();
	private MockTime mockTime = new MockTime(true);
	private MockTimer mockTimer = new MockTimer();
	private MockRequestListener mockListener = new MockRequestListener();
	private MockStreamWriter mockStreamWriter = new MockStreamWriter();

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		MockTcpServerChannel svrChannel = new MockTcpServerChannel();
		mockChanMgr.addTCPSvrChannelToReturn(svrChannel);
        mockChannel.setIncomingFrameDefaultReturnValue(CompletableFuture.completedFuture(mockChannel));
        mockListener.setDefaultRetVal(mockStreamWriter);
        mockStreamWriter.setDefaultRetValToThis();

        Http2Config config = new Http2Config();
        config.setLocalSettings(localSettings);        
		InjectionConfig injConfig = new InjectionConfig(new DirectExecutor(), mockTime, config);

		FrontendConfig frontendConfig = new FrontendConfig("http", new InetSocketAddress("me", 8080));
		HttpFrontendManager manager = HttpFrontendFactory.createFrontEnd(mockChanMgr, mockTimer, injConfig);
		HttpServer httpServer = manager.createHttpServer(frontendConfig, mockListener);
		httpServer.start();
        
		ConnectionListener listener = mockChanMgr.getSingleConnectionListener();
		CompletableFuture<DataListener> futureList = listener.connected(mockChannel, true);
		DataListener dataListener = futureList.get(3, TimeUnit.SECONDS);
		mockChannel.setDataListener(dataListener);
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
//		//verify settings on connect were sent
//		List<Http2Msg> frames = mockChannel.getFramesAndClear();
//		Preface preface = (Preface) frames.get(0);
//		preface.verify();
//		Http2Msg settings1 = frames.get(1);
//		Assert.assertEquals(HeaderSettings.createSettingsFrame(localSettings), settings1);
//		
//		//server's settings frame is finally coming in as well with maxConcurrent=1
//		HeaderSettings settings = new HeaderSettings();
//		settings.setMaxConcurrentStreams(1L);
//		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
//		mockChannel.write(new SettingsFrame(true)); //ack client frame
//		
//		SettingsFrame clientAck = (SettingsFrame) mockChannel.getFrameAndClear();
//		Assert.assertEquals(true, clientAck.isAck());
	}
	
	@Test
	public void testSection3_4WithH2cTokenFailsIfHttp1_1() {
		//do we want to implement?  basically some weird parse error happens and we could catch 
		//and say this appears not to be http2 to the client
	}
	
	
}
