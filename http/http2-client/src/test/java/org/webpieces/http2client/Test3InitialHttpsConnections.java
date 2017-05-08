package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLEngine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.integ.ForTestSslClientEngineFactory;
import org.webpieces.http2client.mock.MockChanMgr;
import org.webpieces.http2client.mock.MockHttp2Channel;
import org.webpieces.http2client.mock.Preface;
import org.webpieces.util.threading.DirectExecutor;

import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

/**
 * Test this section of rfc..
 * http://httpwg.org/specs/rfc7540.html#starting
 */
public class Test3InitialHttpsConnections {

	private MockChanMgr mockChanMgr;
	private MockHttp2Channel mockChannel;
	private Http2Socket socket;
	private HeaderSettings localSettings = Requests.createSomeSettings();

	@Before
	public void setUp() throws InterruptedException, ExecutionException {
		
        mockChanMgr = new MockChanMgr();
        mockChannel = new MockHttp2Channel();
        mockChannel.setIncomingFrameDefaultReturnValue(CompletableFuture.completedFuture(mockChannel));

        Http2Config config = new Http2Config();
        config.setInitialRemoteMaxConcurrent(1); //start with 1 max concurrent
        config.setLocalSettings(localSettings);
        Http2Client client = Http2ClientFactory.createHttpClient(config, mockChanMgr, new DirectExecutor());
        
        mockChanMgr.addSSLChannelToReturn(mockChannel);
		
        InetSocketAddress addr = new InetSocketAddress("somehost.com", 555);
		String host = addr.getHostName();
		int port = addr.getPort();
		ForTestSslClientEngineFactory ssl = new ForTestSslClientEngineFactory();
		SSLEngine engine = ssl.createSslEngine(host, port);
		socket = client.createHttpsSocket("simple", engine);
		
		CompletableFuture<Http2Socket> connect = socket.connect(addr);
		Assert.assertTrue(connect.isDone());
		Assert.assertEquals(socket, connect.get());

		//verify settings on connect were sent
		//verify settings on connect were sent
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Preface preface = (Preface) frames.get(0);
		preface.verify();
		Http2Msg settings1 = frames.get(1);
		Assert.assertEquals(HeaderSettings.createSettingsFrame(localSettings), settings1);
	}

	/**
	 * Works with everyone but incurs a round trip overhead
	 * 
	 * client sends PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n after TLS ALPN
	 * 
	 * The first HTTP/2 frame sent by the server MUST be a server connection preface (Section 3.5) 
	 * consisting of a SETTINGS frame (Section 6.5). Upon 
	 * receiving the 101 response, the client MUST send a connection preface
	 *  (Section 3.5), which includes a SETTINGS frame.
	 */
	@Test
	public void testSection3_3WithH2andAlpn() {
		
	}
	
	/**
	 * Only will work with webpieces and jetty or webservers that support prior knowledge
	 * 
	 * should send PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n
	 * and server sends back it's preface...
	 * The server connection preface consists of a potentially empty SETTINGS 
	 * frame (Section 6.5) that MUST be the first frame the server sends in the HTTP/2 connection.
	 */
	@Test
	public void testSection3_3WithPriorKnowledge() {
		//this is actually tested with the http version of this test
	}
	
	@Test
	public void testSection3_3WithH2PriorKnowledgeFailsIfHttp1_1() {
		//same as http version of this test
	}
	
}
