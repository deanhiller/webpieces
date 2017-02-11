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
import org.webpieces.http2client.api.Http2SocketDataWriter;
import org.webpieces.http2client.mock.MockChanMgr;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.mock.MockServerListener;
import org.webpieces.http2client.mock.MockHttp2Channel;
import org.webpieces.http2client.mock.SocketWriter;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class TestBasicHttp2Client {

	private MockChanMgr mockChanMgr;
	private MockHttp2Channel mockChannel;
	private Http2Socket socket;
	private SocketWriter socketWriter;
	private HeaderSettings localSettings = Requests.createSomeSettings();

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
		
		MockServerListener mockSvrListener = new MockServerListener();
		CompletableFuture<Http2Socket> connect = socket.connect(new InetSocketAddress(555), mockSvrListener);
		Assert.assertTrue(connect.isDone());
		Assert.assertEquals(socket, connect.get());

		//verify settings on connect were sent
		Http2Msg clientSettings = mockChannel.getFrameAndClear();
		Assert.assertEquals(HeaderSettings.createSettingsFrame(localSettings), clientSettings);
		
		socketWriter = mockChannel.getSocketWriter();
		
		//server's settings frame is coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(1L);
		socketWriter.write(HeaderSettings.createSettingsFrame(settings));
		socketWriter.write(new SettingsFrame(true)); //ack client frame
		SettingsFrame ack = (SettingsFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(true, ack.isAck());
	}
	
	@Test
	public void testBasicIntegration() throws InterruptedException, ExecutionException {
		Http2Headers request1 = Requests.createRequest();
		Http2Headers request2 = Requests.createRequest();

		MockResponseListener clientResponseListener1 = new MockResponseListener();
		clientResponseListener1.setIncomingRespDefault(CompletableFuture.completedFuture(null));
		MockResponseListener listener2 = new MockResponseListener();
		CompletableFuture<Http2SocketDataWriter> future = socket.sendRequest(request1, clientResponseListener1);
		CompletableFuture<Http2SocketDataWriter> future2 = socket.sendRequest(request2, listener2);
		
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(1, req.getStreamId());
		Assert.assertEquals(request1, req);
		
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future2.isDone());
		
		Http2Headers resp1 = Requests.createResponse(request1.getStreamId());
		socketWriter.write(resp1); //endOfStream=false
		PartialStream response1 = clientResponseListener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(resp1, response1);
		
		Assert.assertFalse(future2.isDone());
		socketWriter.write(new DataFrame(request1.getStreamId(), false)); //endOfStream=false
		clientResponseListener1.getSingleReturnValueIncomingResponse();
		
		//at this point, should not have a call outstanding
		mockChannel.assertNoIncomingMessages();
		
		clientResponseListener1.addReturnValueIncomingResponse(CompletableFuture.completedFuture(null));
		
		Assert.assertFalse(future2.isDone());
		socketWriter.write(new DataFrame(request1.getStreamId(), true));//endOfStream = true
		Assert.assertTrue(future2.isDone());
		
		clientResponseListener1.getSingleReturnValueIncomingResponse();
		
		Http2Msg frame = mockChannel.getFrameAndClear();
		Assert.assertEquals(3, frame.getStreamId());
	}
	
}
