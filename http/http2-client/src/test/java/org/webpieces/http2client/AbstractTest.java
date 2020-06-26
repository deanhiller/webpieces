package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.SettingsFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class AbstractTest {
	protected static final DataWrapperGenerator DATA_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	protected MockChanMgr mockChanMgr = new MockChanMgr();
	protected MockHttp2Channel mockChannel = new MockHttp2Channel();
	protected HeaderSettings localSettings = Requests.createSomeSettings();
	protected MockTime mockTime = new MockTime(true);

	protected Http2Socket httpSocket;

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
        mockChannel.setIncomingFrameDefaultReturnValue(CompletableFuture.completedFuture(null));

        Http2Config config = new Http2Config();
        config.setInitialRemoteMaxConcurrent(1); //start with 1 max concurrent
        localSettings.setInitialWindowSize(localSettings.getMaxFrameSize()*4);
        config.setLocalSettings(localSettings);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		InjectionConfig injConfig = new InjectionConfig(mockTime, config, metrics);
        Http2Client client = Http2ClientFactory.createHttpClient("test2Client", mockChanMgr, injConfig);
        
        mockChanMgr.addTCPChannelToReturn(mockChannel);
		httpSocket = client.createHttpSocket(new SocketListener());
		
		CompletableFuture<Void> connect = httpSocket.connect(new InetSocketAddress(555));
		connect.get(2, TimeUnit.SECONDS);

		//clear preface and settings frame from client
		mockChannel.getFramesAndClear();
		
		//server's settings frame is finally coming in as well with maxConcurrent=1
		sendAndAckSettingsFrame(1);
	}
	
	private void sendAndAckSettingsFrame(long max) throws InterruptedException, ExecutionException {
		//server's settings frame is finally coming in as well with maxConcurrent=1
		HeaderSettings settings = new HeaderSettings();
		settings.setMaxConcurrentStreams(max);
		mockChannel.write(HeaderSettings.createSettingsFrame(settings));
		mockChannel.write(new SettingsFrame(true)); //ack client frame
		SettingsFrame ack = (SettingsFrame) mockChannel.getFrameAndClear();
		Assert.assertEquals(true, ack.isAck());
	}
	
	protected void sendPushPromise(MockResponseListener listener1, int streamId, boolean eos) {
		MockPushListener pushListener = new MockPushListener();
		
		pushListener.setDefaultResponse(CompletableFuture.completedFuture(null));
		listener1.addReturnValuePush(pushListener);
		Http2Push push = Requests.createPush(streamId);
		mockChannel.write(push); //endOfStream=false
		Assert.assertEquals(push, listener1.getSinglePush());
		
		Http2Response preemptiveResponse = Requests.createEosResponse(2);
		mockChannel.write(preemptiveResponse);
		
		Http2Response frame2 = (Http2Response) pushListener.getSingleParam();
		Assert.assertEquals(preemptiveResponse, frame2);
		
	}
	
	protected void sendResponseFromServer(MockResponseListener listener1, Http2Request request) {
		Http2Response resp1 = Requests.createResponse(request.getStreamId());
		mockChannel.write(resp1); //endOfStream=false
		Http2Response response1 = listener1.getSingleReturnValueIncomingResponse();
		Assert.assertEquals(resp1, response1);
	}

	protected Http2Request sendRequestToServer(MockResponseListener listener1) {
		Http2Request request1 = Requests.createRequest();

		httpSocket.openStream().process(request1, listener1);
		
		Http2Msg req = mockChannel.getFrameAndClear();
		Assert.assertEquals(request1, req);
		return request1;
	}
}
