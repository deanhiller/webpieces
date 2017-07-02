package org.webpieces.nio.api.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLEngine;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.mocks.MockClientSideJdkChannel;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.nio.api.mocks.MockSslDataListener;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SSLParser;
import org.webpieces.ssl.api.SslResult;
import org.webpieces.ssl.api.SslState;
import org.webpieces.util.threading.DirectExecutor;

public class TestBasicSsl {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private MockSslDataListener mockClientDataListener = new MockSslDataListener();
	
	private MockClientSideJdkChannel mockChannel = new MockClientSideJdkChannel();
	private MockJdk mockJdk = new MockJdk(mockChannel);

	private SSLParser svrSslParser;
	
	@Ignore
	@Test
	public void testBasic() throws InterruptedException, ExecutionException, TimeoutException, GeneralSecurityException, IOException {
		MockSSLEngineFactory sslFactory = new MockSSLEngineFactory();	
		BufferPool pool = new BufferCreationPool(false, 17000, 1000);
		SSLEngine clientSsl = sslFactory.createEngineForSocket();
		SSLEngine svrSsl = sslFactory.createEngineForServerSocket();
		svrSslParser = AsyncSSLFactory.create("svr", svrSsl, pool);

		ChannelManager chanMgr = createSvrChanMgr("server");
		
		int port = 8443;
		
		TCPChannel channel = chanMgr.createTCPChannel("client", clientSsl);
		
		mockChannel.setNumBytesToConsume(100000);
		mockChannel.addConnectReturnValue(true);
		mockJdk.setThread(Thread.currentThread()); //trick the selector into thinking we are on the selector thread
		CompletableFuture<Void> future = channel.connect(new InetSocketAddress("localhost", port), mockClientDataListener);
		Assert.assertFalse(future.isDone()); //not connected until ssl handshake is complete

		byte[] payload = mockChannel.nextPayload();
		DataWrapper dataWrapper = dataGen.wrapByteArray(payload);
		CompletableFuture<SslResult> resultFuture = svrSslParser.parseIncoming(dataWrapper);
		SslResult result = resultFuture.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(SslState.SEND_TO_SOCKET, result.getSslState());
		
		DataWrapper encryptedData = result.getEncryptedData();
		mockChannel.forceDataRead(mockJdk, encryptedData.createByteArray());

		future.get(2, TimeUnit.SECONDS);
	}

	private ChannelManager createSvrChanMgr(String name) {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk);
		ChannelManager svrMgr = factory.createMultiThreadedChanMgr(name+"Mgr", new BufferCreationPool(), new BackpressureConfig(), new DirectExecutor());
		return svrMgr;
	}
}
