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
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.util.threading.DirectExecutor;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class TestErrors {

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
        
        mockChanMgr.addTCPChannelToReturn(mockChannel);
		socket = client.createHttpSocket("simple");
		
		CompletableFuture<Http2Socket> connect = socket.connect(new InetSocketAddress(555));
		Assert.assertTrue(connect.isDone());
		Assert.assertEquals(socket, connect.get());

		//clear expected preface and settings
		mockChannel.getFramesAndClear();
		
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(1L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.write(new SettingsFrame(true)); //ack client frame
		
		Http2Msg svrSettings = mockChannel.getFrameAndClear();
		SettingsFrame expected = new SettingsFrame(true);
		Assert.assertEquals(expected, svrSettings);
	}
	
	@Test
	public void testBadServerSendsInvalidResponseStreamIdWrong() throws InterruptedException, ExecutionException {
		Http2Headers request1 = Requests.createRequest();

		MockResponseListener listener1 = new MockResponseListener();
		socket.send(request1, listener1);

		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(request1, req);

		//mockChannel.write(Requests.createResponse(0)); //endOfStream=false

		//TODO: ensure GOAWAY FRAME HERE actually
//		Http2Msg msgFromClient = mockChannel.getFrameAndClear();
//		RstStreamFrame str = new RstStreamFrame();
//		Assert.assertEquals(str, msgFromClient);
	}

}
