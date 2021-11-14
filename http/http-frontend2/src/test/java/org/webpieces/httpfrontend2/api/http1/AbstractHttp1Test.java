package org.webpieces.httpfrontend2.api.http1;

import java.net.InetSocketAddress;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.httpfrontend2.api.http2.Http2Requests;
import org.webpieces.httpfrontend2.api.mock2.MockChanMgr;
import org.webpieces.httpfrontend2.api.mock2.MockHttp1Channel;
import org.webpieces.httpfrontend2.api.mock2.MockHttp2RequestListener;
import org.webpieces.httpfrontend2.api.mock2.MockStreamWriter;
import org.webpieces.httpfrontend2.api.mock2.MockTcpServerChannel;
import org.webpieces.mock.time.MockTime;
import org.webpieces.mock.time.MockTimer;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class AbstractHttp1Test {
	protected static final DataWrapperGenerator DATA_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	protected MockChanMgr mockChanMgr = new MockChanMgr();
	protected MockHttp1Channel mockChannel = new MockHttp1Channel();
	protected HeaderSettings localSettings = Http2Requests.createSomeSettings();
	protected MockTime mockTime = new MockTime(true);
	protected MockTimer mockTimer = new MockTimer();
	protected MockHttp2RequestListener mockListener = new MockHttp2RequestListener();
	protected MockStreamWriter mockStreamWriter = new MockStreamWriter();
	
	@Before
	public void setUp() throws InterruptedException, ExecutionException, TimeoutException {
		MockTcpServerChannel svrChannel = new MockTcpServerChannel();
		mockChanMgr.addTCPSvrChannelToReturn(svrChannel);
        mockChannel.setIncomingFrameDefaultReturnValue(XFuture.completedFuture(null));
        mockListener.setDefaultRetVal(mockStreamWriter);
        mockStreamWriter.setDefaultRetValToThis();

        Http2Config config = new Http2Config();
        config.setLocalSettings(localSettings);
        SimpleMeterRegistry metrics = new SimpleMeterRegistry();
		InjectionConfig injConfig = new InjectionConfig(mockTime, config, metrics);

		HttpSvrConfig frontendConfig = new HttpSvrConfig("http", new InetSocketAddress("me", 8080));
		HttpFrontendManager manager = HttpFrontendFactory.createFrontEnd(mockChanMgr, mockTimer, injConfig, Metrics.globalRegistry);
		HttpServer httpServer = manager.createHttpServer(frontendConfig, mockListener);
		httpServer.start();
        
		simulateClientConnecting();
	}

	private void simulateClientConnecting() throws InterruptedException, ExecutionException, TimeoutException {
		ConnectionListener listener = mockChanMgr.getSingleConnectionListener();
		XFuture<DataListener> futureList = listener.connected(mockChannel, true);
		DataListener dataListener = futureList.get(3, TimeUnit.SECONDS);
		mockChannel.setDataListener(dataListener);
	}
	
}
