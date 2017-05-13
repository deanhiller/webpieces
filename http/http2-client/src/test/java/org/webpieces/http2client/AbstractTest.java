package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.mock.MockChanMgr;
import org.webpieces.http2client.mock.MockHttp2Channel;
import org.webpieces.http2client.mock.MockPushListener;
import org.webpieces.http2client.mock.MockResponseListener;
import org.webpieces.http2client.util.Requests;
import org.webpieces.mock.time.MockTime;
import org.webpieces.util.threading.DirectExecutor;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class AbstractTest {
	protected static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	protected MockChanMgr mockChanMgr = new MockChanMgr();
	protected MockHttp2Channel mockChannel = new MockHttp2Channel();
	protected HeaderSettings localSettings = Requests.createSomeSettings();
	protected MockTime mockTime = new MockTime(true);

	protected Http2Socket httpSocket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException {
        mockChannel.setIncomingFrameDefaultReturnValue(CompletableFuture.completedFuture(mockChannel));

        Http2Config config = new Http2Config();
        config.setInitialRemoteMaxConcurrent(1); //start with 1 max concurrent
        localSettings.setInitialWindowSize(localSettings.getMaxFrameSize()*4);
        config.setLocalSettings(localSettings);
		InjectionConfig injConfig = new InjectionConfig(new DirectExecutor(), mockTime, config);
        Http2Client client = Http2ClientFactory.createHttpClient(mockChanMgr, injConfig);
        
        mockChanMgr.addTCPChannelToReturn(mockChannel);
		httpSocket = client.createHttpSocket("simple");
		
		CompletableFuture<Http2Socket> connect = httpSocket.connect(new InetSocketAddress(555));
		Assert.assertTrue(connect.isDone());
		Assert.assertEquals(httpSocket, connect.get());

		//clear preface and settings frame from client
		mockChannel.getFramesAndClear();
		
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(1L);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		SettingsFrame ack = (SettingsFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(true, ack.isAck());
	}
	
	protected void sendPushPromise(MockResponseListener listener1, MockPushListener pushListener, int streamId, boolean eos) {
		pushListener.setDefaultResponse(CompletableFuture.completedFuture(null));
		listener1.addReturnValuePush(pushListener);
		Http2Push push = Requests.createPush(streamId);
		mockChannel.write(push); //endOfStream=false
		Assert.assertEquals(2, listener1.getSinglePushStreamId());
		
		Http2Push frame = (Http2Push) pushListener.getSingleParam();
		Assert.assertEquals(push, frame);
		
		Http2Headers preemptiveResponse = Requests.createEosResponse(2);
		mockChannel.write(preemptiveResponse);
		
		Http2Headers frame2 = (Http2Headers) pushListener.getSingleParam();
		Assert.assertEquals(preemptiveResponse, frame2);
		
	}
	
	protected void sendResponseFromServer(MockResponseListener listener1, Http2Headers request) {
		Http2Headers resp1 = Requests.createResponse(request.getStreamId());
		mockChannel.write(resp1); //endOfStream=false
		PartialStream response1 = listener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(resp1, response1);
	}

	protected Http2Headers sendRequestToServer(MockResponseListener listener1) {
		Http2Headers request1 = Requests.createRequest();

		httpSocket.send(request1, listener1);
		
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(request1, req);
		return request1;
	}
}
