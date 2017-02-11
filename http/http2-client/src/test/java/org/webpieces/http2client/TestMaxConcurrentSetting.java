package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.List;
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

public class TestMaxConcurrentSetting {

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
		Http2Msg settings = mockChannel.getFrameAndClear();
		Assert.assertEquals(HeaderSettings.createSettingsFrame(localSettings), settings);
		
		socketWriter = mockChannel.getSocketWriter();
	}
	
	@Test
	public void testSend2ndRequestOnlyOnCompletionOfFirst() throws InterruptedException, ExecutionException {
		RequestsSent sent = sendTwoRequests();

		sendAndAckSettingsFrame(1L);

		int streamId1 = sent.getRequest1().getRequest().getStreamId();
		CompletableFuture<Http2SocketDataWriter> future2 = sent.getRequest2().getFuture();
		MockResponseListener listener1 = sent.getRequest1().getListener(); 
		
		sendHeadersAndData(streamId1, future2, listener1);
		
		listener1.getSingleReturnValueIncomingResponse();

		Http2Headers frame = (Http2Headers) mockChannel.getFrameAndClear();
		Assert.assertEquals(sent.getRequest2().getRequest(), frame);
		Assert.assertTrue(future2.isDone());
	}
	
	@Test
	public void testSend2ndRequestOnlyOnAfterSettingsFrameMaxConcurrentBigger() throws InterruptedException, ExecutionException {
		RequestsSent sent = sendTwoRequests();

		Assert.assertFalse(sent.getRequest2().getFuture().isDone());

		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(2L);
		socketWriter.write(HeaderSettings.createSettingsFrame(settings));
		socketWriter.write(new SettingsFrame(true)); //ack client frame
		List<Http2Msg> msgs = mockChannel.getFramesAndClear();
		
		Assert.assertEquals(sent.getRequest2().getRequest(), msgs.get(0));
		Assert.assertTrue(sent.getRequest2().getFuture().isDone());
		
		SettingsFrame ack = (SettingsFrame) msgs.get(1);
		Assert.assertEquals(true, ack.isAck());
	}
	
	private void sendHeadersAndData(int streamId1, CompletableFuture<Http2SocketDataWriter> future2,
			MockResponseListener listener1) throws InterruptedException, ExecutionException {
		socketWriter.write(Requests.createResponse(streamId1)); //endOfStream=false
		listener1.getSingleReturnValueIncomingResponse();
				
		socketWriter.write(new DataFrame(streamId1, false)); //endOfStream=false
		listener1.getSingleReturnValueIncomingResponse();
		
		//at this point, should not have a call outstanding
		mockChannel.assertNoIncomingMessages();
		Assert.assertFalse(future2.isDone());

		listener1.addReturnValueIncomingResponse(CompletableFuture.completedFuture(null));
		Assert.assertFalse(future2.isDone());
		socketWriter.write(new DataFrame(streamId1, true));//endOfStream = true
	}
	
	private RequestsSent sendTwoRequests() {
		Http2Headers request1 = Requests.createRequest();
		Http2Headers request2 = Requests.createRequest();
		MockResponseListener listener1 = new MockResponseListener();
		MockResponseListener listener2 = new MockResponseListener();

		listener1.setIncomingRespDefault(CompletableFuture.completedFuture(null));
		CompletableFuture<Http2SocketDataWriter> future = socket.sendRequest(request1, listener1);
		CompletableFuture<Http2SocketDataWriter> future2 = socket.sendRequest(request2, listener2);
		
		RequestHolder r1 = new RequestHolder(request1, listener1, future);
		RequestHolder r2 = new RequestHolder(request2, listener2, future2);		
		RequestsSent requests = new RequestsSent(r1, r2);
		
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(1, req.getStreamId());
		Assert.assertEquals(request1, req);
		
		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future2.isDone());
		return requests;
	}

	private void sendAndAckSettingsFrame(long max) throws InterruptedException, ExecutionException {
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(max);
		socketWriter.write(HeaderSettings.createSettingsFrame(settings));
		socketWriter.write(new SettingsFrame(true)); //ack client frame
		SettingsFrame ack = (SettingsFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(true, ack.isAck());
	}
}
