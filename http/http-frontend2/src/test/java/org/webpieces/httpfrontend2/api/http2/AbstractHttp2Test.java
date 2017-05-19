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
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
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
import org.webpieces.util.threading.DirectExecutor;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class AbstractHttp2Test {
	protected static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	protected MockChanMgr mockChanMgr = new MockChanMgr();
	protected MockHttp2Channel mockChannel = new MockHttp2Channel();
	protected HeaderSettings localSettings = Http2Requests.createSomeSettings();
	protected MockTime mockTime = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();
	protected MockHttp2RequestListener mockListener = new MockHttp2RequestListener();
	protected MockStreamWriter mockStreamWriter = new MockStreamWriter();

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
        
		simulateClientConnecting();
		
		simulateClientSendingPrefaceAndSettings();
	}

	private void simulateClientConnecting() throws InterruptedException, ExecutionException, TimeoutException {
		ConnectionListener listener = mockChanMgr.getSingleConnectionListener();
		CompletableFuture<DataListener> futureList = listener.connected(mockChannel, true);
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
		Http2Headers request1 = Http2Requests.createRequest(streamId, eos);

		mockChannel.send(request1);
		
		PassedIn req = mockListener.getSingleRequest();
		Assert.assertEquals(request1, req.request);
		return req;
	}
}
