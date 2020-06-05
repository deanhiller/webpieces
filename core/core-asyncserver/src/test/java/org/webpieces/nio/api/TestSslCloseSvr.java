package org.webpieces.nio.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLEngine;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.TwoPools;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.mocks.MockAsyncListener;
import org.webpieces.nio.api.mocks.MockAsyncListener.ConnectionOpen;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.nio.api.mocks.MockSvrChannel;
import org.webpieces.nio.api.mocks.MockSvrSideJdkChannel;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SSLMetrics;
import org.webpieces.ssl.api.SSLParser;
import org.webpieces.ssl.api.dto.SslAction;
import org.webpieces.ssl.api.dto.SslActionEnum;
import org.webpieces.util.threading.DirectExecutor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestSslCloseSvr {

	//private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private MockAsyncListener listener = new MockAsyncListener();
	private MockSvrChannel mockSvrChannel = new MockSvrChannel();
	private MockSvrSideJdkChannel mockChannel = new MockSvrSideJdkChannel();
	private MockJdk mockJdk = new MockJdk(mockSvrChannel);

	private SSLParser clientSslParser;

	private TCPChannel channel;

	private AsyncServer server;

	@Before
	public void setup() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		System.setProperty("jdk.tls.server.protocols", "TLSv1.2");
		System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

		server = createServer();
		clientSslParser = createClientParser();
		
		CompletableFuture<Void> future = server.start(new InetSocketAddress(8443));
		Assert.assertFalse(future.isDone());
		
		mockJdk.setThread(Thread.currentThread());
		mockJdk.fireSelector();
		future.get(2, TimeUnit.SECONDS);

		SslAction result = clientSslParser.beginHandshake();
		
		//simulate the jdk firing the selector with a new channel...
		mockSvrChannel.addNewChannel(mockChannel);
		mockJdk.setThread(Thread.currentThread());
		mockJdk.fireSelector();

		//assert connectionOpened was called with value of isReadyForWrites=false 
		//(This feature is specifically so clients can start a time and timeout the connection if they do not
		//receive a valid payload in a certain amount of time).
		ConnectionOpen connectionOpenedInfo = listener.getConnectionOpenedInfo();
		channel = connectionOpenedInfo.channel;
		Assert.assertEquals(false, connectionOpenedInfo.isReadyForWrites);
		
		mockChannel.setNumBytesToConsume(100000);
		
		mockChannel.forceDataRead(mockJdk, result.getEncryptedData());
		
		SslAction action = parseIncoming(); //3 encrypted packets sent here
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());
		
		mockChannel.forceDataRead(mockJdk, action.getEncryptedData());
		
		Assert.assertEquals(SslActionEnum.WAIT_FOR_MORE_DATA_FROM_REMOTE_END, parseIncoming().getSslAction());

		Assert.assertEquals(SslActionEnum.SEND_LINK_ESTABLISHED_TO_APP, parseIncoming().getSslAction());

		ConnectionOpen openedInfo = listener.getConnectionOpenedInfo();
		Assert.assertEquals(true, openedInfo.isReadyForWrites);
		Assert.assertEquals(channel, openedInfo.channel);
		
		transferBigData();
	}

	@After
	public void teardown() {
		System.clearProperty("jdk.tls.server.protocols");
		System.clearProperty("jdk.tls.client.protocols");
	}

	private SSLParser createClientParser() {
		SSLEngineFactoryForTest sslFactory = new SSLEngineFactoryForTest();	
		BufferPool pool = new TwoPools("p1", new SimpleMeterRegistry());
		SSLEngine clientSsl = sslFactory.createEngineForSocket();
		SSLMetrics sslMetrics = new SSLMetrics("", new SimpleMeterRegistry());
		SSLParser clientSslParser1 = AsyncSSLFactory.create("svr", clientSsl, pool, sslMetrics);
		return clientSslParser1;
	}

	private AsyncServer createServer() {
		MeterRegistry meters = Metrics.globalRegistry;
		SSLEngineFactoryForTest sslFactory = new SSLEngineFactoryForTest();	
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk, meters);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("test'n", new TwoPools("pl", new SimpleMeterRegistry()), new BackpressureConfig(), new DirectExecutor());
		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(mgr, meters);
		AsyncServer server1 = svrMgr.createTcpServer(new AsyncConfig(), listener, sslFactory);
		return server1;
	}

	private void transferBigData() throws InterruptedException, ExecutionException, TimeoutException {
		ByteBuffer b = ByteBuffer.allocate(17000);
		b.put((byte) 1);
		b.put((byte) 2);
		b.position(b.limit()-2); //simulate buffer full of 0's except first 2 and last 2
		b.put((byte) 3);
		b.put((byte) 4);
		b.flip();
		
		CompletableFuture<Void> future = channel.write(b);
		future.get(2, TimeUnit.SECONDS);

		//results in two ssl packets going out instead of the one that was fed in..
		SslAction action = parseIncoming();
		SslAction action2 = parseIncoming();
		Assert.assertEquals(SslActionEnum.SEND_TO_APP, action.getSslAction());
		Assert.assertEquals(SslActionEnum.SEND_TO_APP, action2.getSslAction());
		
		Assert.assertEquals(17000, action.getDecryptedData().getReadableSize()+action2.getDecryptedData().getReadableSize());
	}
	
	@Test
	public void testBasicCloseFromServer() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<Void> future = channel.close();
		Assert.assertTrue(future.isDone());

		DataWrapper payload = mockChannel.nextPayload();
		CompletableFuture<List<SslAction>> resultFuture2 = clientSslParser.parseIncoming(payload);
		List<SslAction> result2 = resultFuture2.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(2, result2.size());

		Assert.assertTrue(future.isDone());
		
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, result2.get(0).getSslAction());
		Assert.assertEquals(SslActionEnum.SEND_LINK_CLOSED_TO_APP, result2.get(1).getSslAction());
		
		mockChannel.forceDataRead(mockJdk, result2.get(0).getEncryptedData());

		future.get(2, TimeUnit.SECONDS);
	}
	
	@Test
	public void testBasicCloseFromClient() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		SslAction action = clientSslParser.close();
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());

		mockChannel.forceDataRead(mockJdk, action.getEncryptedData());

		Channel closedChannel = listener.getConnectionClosed();
		Assert.assertEquals(channel, closedChannel);

		SslAction action2 = parseIncoming();
		Assert.assertEquals(SslActionEnum.LINK_SUCCESSFULLY_CLOSED, action2.getSslAction());
	}
	
	@Test
	public void testBothEndsAtSameTime() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<Void> future = channel.close();
		SslAction action = clientSslParser.close();
		
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());
		Assert.assertTrue(future.isDone());
		
		mockChannel.forceDataRead(mockJdk, action.getEncryptedData());
		future.get(2, TimeUnit.SECONDS);

		SslAction action2 = parseIncoming();
		Assert.assertEquals(SslActionEnum.LINK_SUCCESSFULLY_CLOSED, action2.getSslAction());
		
		//far end closed should NOT be called...
		Assert.assertEquals(0, listener.getNumConnectionsClosed());
	}
	
	@Test
	public void testRaceFarendCloseThenServerCloses() throws InterruptedException, ExecutionException, TimeoutException {
		SslAction action = clientSslParser.close();
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());

		mockChannel.forceDataRead(mockJdk, action.getEncryptedData());
		Assert.assertEquals(1, listener.getNumConnectionsClosed());

		SslAction action2 = parseIncoming();
		Assert.assertEquals(SslActionEnum.LINK_SUCCESSFULLY_CLOSED, action2.getSslAction());

		//but before the client knew it was closing and was notified, it calls close as well
		CompletableFuture<Void> future = channel.close();
		future.get(2, TimeUnit.SECONDS);
	}
	
	private SslAction parseIncoming() throws InterruptedException, ExecutionException, TimeoutException {
		DataWrapper payload = mockChannel.nextPayload();
		CompletableFuture<List<SslAction>> resultFuture2 = clientSslParser.parseIncoming(payload);
		List<SslAction> result2 = resultFuture2.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(1, result2.size());
		return result2.get(0);
	}
	
}
