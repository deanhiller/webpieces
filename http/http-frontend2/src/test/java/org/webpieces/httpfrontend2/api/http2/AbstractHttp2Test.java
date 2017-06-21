package org.webpieces.httpfrontend2.api.http2;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.httpfrontend2.api.mock2.Http2ChannelCache;
import org.webpieces.httpfrontend2.api.mock2.MockChanMgr;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2Channel;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener.PassedIn;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;
import org.webpieces.httpfrontend2.api.mock2.MockTcpServerChannel;
import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class AbstractHttp2Test {
	protected static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	protected MockChanMgr mockChanMgr = new MockChanMgr();
	protected Http2ChannelCache mockTcpChannel = new Http2ChannelCache();
	protected MockHttp2Channel mockChannel = new MockHttp2Channel(mockTcpChannel);
	protected HeaderSettings localSettings = Http2Requests.createSomeSettings();
	protected MockTime mockTime = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();
	protected MockHttp2RequestListener mockListener = new MockHttp2RequestListener();
	protected MockStreamWriter mockStreamWriter = new MockStreamWriter();

	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		MockTcpServerChannel svrChannel = new MockTcpServerChannel();
		mockChanMgr.addTCPSvrChannelToReturn(svrChannel);
		mockTcpChannel.setIncomingFrameDefaultReturnValue(CompletableFuture.completedFuture(null));
        mockListener.setDefaultRetVal(mockStreamWriter);
        mockStreamWriter.setDefaultRetValToThis();

        Http2Config config = new Http2Config();
        config.setLocalSettings(localSettings);
		InjectionConfig injConfig = new InjectionConfig(mockTime, config);

		HttpSvrConfig frontendConfig = new HttpSvrConfig("http", new InetSocketAddress("me", 8080));
		HttpFrontendManager manager = HttpFrontendFactory.createFrontEnd(mockChanMgr, mockTimer, injConfig);
		HttpServer httpServer = manager.createHttpServer(frontendConfig, mockListener);
		httpServer.start();
        
		simulateClientConnecting();
		
		simulateClientSendingPrefaceAndSettings();
	}

	private void simulateClientConnecting() throws InterruptedException, ExecutionException, TimeoutException {
		ConnectionListener listener = mockChanMgr.getSingleConnectionListener();
		CompletableFuture<DataListener> futureList = listener.connected(mockTcpChannel, true);
		DataListener dataListener = futureList.get(3, TimeUnit.SECONDS);
		mockChannel.setDataListener(dataListener);
	}

	protected void simulateClientSendingPrefaceAndSettings() {
		//this method is overloaded by one test
		HeaderSettings settings = Http2Requests.createSomeSettings();
		mockChannel.sendPrefaceAndSettings(HeaderSettings.createSettingsFrame(settings));
		List<Http2Msg> frames = mockChannel.getFramesAndClear();
		Assert.assertEquals(2, frames.size());
	}
	
	protected PassedIn sendRequestToServer(int streamId, boolean eos) {
		Http2Request request1 = Http2Requests.createRequest(streamId, eos);

		mockChannel.send(request1);
		
		PassedIn req = mockListener.getSingleRequest();
		Assert.assertEquals(request1, req.request);
		return req;
	}
}
