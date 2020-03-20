package org.webpieces.nio.api.ssl;

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
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.mocks.MockClientSideJdkChannel;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.nio.api.mocks.MockSslDataListener;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SSLParser;
import org.webpieces.ssl.api.dto.SslAction;
import org.webpieces.ssl.api.dto.SslActionEnum;
import org.webpieces.util.threading.DirectExecutor;

import io.micrometer.core.instrument.Metrics;

public class TestSslCloseClient {

	private MockSslDataListener mockClientDataListener = new MockSslDataListener();
	
	private MockClientSideJdkChannel mockChannel = new MockClientSideJdkChannel();
	private MockJdk mockJdk = new MockJdk(mockChannel);

	private SSLParser svrSslParser;

	private TCPChannel channel;

	private CompletableFuture<Void> connectFuture;

	@Before
	public void setup() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		System.setProperty("jdk.tls.server.protocols", "TLSv1.2");
		System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

		svrSslParser = createSslSvrParser();
		channel = createClientChannel("server", mockJdk);
		
		int port = 8443;
		
		mockChannel.setNumBytesToConsume(100000);
		mockChannel.addConnectReturnValue(true);
		mockJdk.setThread(Thread.currentThread()); //trick the selector into thinking we are on the selector thread
		connectFuture = channel.connect(new InetSocketAddress("localhost", port), mockClientDataListener);
		Assert.assertFalse(connectFuture.isDone()); //not connected until ssl handshake is complete

		SslAction result = parseIncoming();
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, result.getSslAction());
		
		mockChannel.forceDataRead(mockJdk, result.getEncryptedData());
		
		Assert.assertEquals(SslActionEnum.WAIT_FOR_MORE_DATA_FROM_REMOTE_END, parseIncoming().getSslAction());
		Assert.assertEquals(SslActionEnum.WAIT_FOR_MORE_DATA_FROM_REMOTE_END, parseIncoming().getSslAction());
		
		DataWrapper payload = mockChannel.nextPayload();
		CompletableFuture<List<SslAction>> resultFuture2 = svrSslParser.parseIncoming(payload);
		List<SslAction> result2 = resultFuture2.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, result2.get(0).getSslAction());
		Assert.assertEquals(SslActionEnum.SEND_LINK_ESTABLISHED_TO_APP, result2.get(1).getSslAction());
		
		Assert.assertFalse(connectFuture.isDone()); //client is still NOT connected yet until the SSL handshake final messages are received
		
		mockChannel.forceDataRead(mockJdk, result2.get(0).getEncryptedData());
		
		connectFuture.get(2, TimeUnit.SECONDS);
		
		transferBigData();
	}

	@After
	public void teardown() {
		System.clearProperty("jdk.tls.server.protocols");
		System.clearProperty("jdk.tls.client.protocols");
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
		SslAction action = svrSslParser.close();
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());

		mockChannel.forceDataRead(mockJdk, action.getEncryptedData());

		Assert.assertTrue(mockClientDataListener.isClosed());

		SslAction action2 = parseIncoming();
		Assert.assertEquals(SslActionEnum.LINK_SUCCESSFULLY_CLOSED, action2.getSslAction());
	}
	
	@Test
	public void testBasicCloseFromClient() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<Void> future = channel.close();
		Assert.assertFalse(future.isDone());

		DataWrapper payload = mockChannel.nextPayload();
		CompletableFuture<List<SslAction>> resultFuture2 = svrSslParser.parseIncoming(payload);
		List<SslAction> result2 = resultFuture2.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(2, result2.size());
		
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, result2.get(0).getSslAction());
		Assert.assertEquals(SslActionEnum.SEND_LINK_CLOSED_TO_APP, result2.get(1).getSslAction());
		
		mockChannel.forceDataRead(mockJdk, result2.get(0).getEncryptedData());
		
		future.get(2, TimeUnit.SECONDS);
	}
	
	@Test
	public void testBothEndsAtSameTime() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<Void> future = channel.close();
		SslAction action = svrSslParser.close();
		
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());
		Assert.assertFalse(future.isDone());
		
		mockChannel.forceDataRead(mockJdk, action.getEncryptedData());
		future.get(2, TimeUnit.SECONDS);

		SslAction action2 = parseIncoming();
		Assert.assertEquals(SslActionEnum.LINK_SUCCESSFULLY_CLOSED, action2.getSslAction());
		
		//far end closed should NOT be called...
		Assert.assertFalse(mockClientDataListener.isClosed());
	}
	
	@Test
	public void testRaceFarendCloseThenClientCloses() throws InterruptedException, ExecutionException, TimeoutException {
		SslAction action = svrSslParser.close();
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());

		mockChannel.forceDataRead(mockJdk, action.getEncryptedData());
		Assert.assertTrue(mockClientDataListener.isClosed());

		SslAction action2 = parseIncoming();
		Assert.assertEquals(SslActionEnum.LINK_SUCCESSFULLY_CLOSED, action2.getSslAction());

		//but before the client knew it was closing and was notified, it calls close as well
		CompletableFuture<Void> future = channel.close();
		future.get(2, TimeUnit.SECONDS);
	}
	
	private SslAction parseIncoming() throws InterruptedException, ExecutionException, TimeoutException {
		DataWrapper payload = mockChannel.nextPayload();
		CompletableFuture<List<SslAction>> resultFuture2 = svrSslParser.parseIncoming(payload);
		List<SslAction> result2 = resultFuture2.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(1, result2.size());
		return result2.get(0);
	}

	public static TCPChannel createClientChannel(String name, MockJdk mockJdk) throws GeneralSecurityException, IOException {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk, Metrics.globalRegistry);
		ChannelManager chanMgr = factory.createMultiThreadedChanMgr(name+"Mgr", new BufferCreationPool(), new BackpressureConfig(), new DirectExecutor());

		MockSSLEngineFactory sslFactory = new MockSSLEngineFactory();
		SSLEngine clientSsl = sslFactory.createEngineForSocket();	
		TCPChannel channel1 = chanMgr.createTCPChannel("client", clientSsl);
		return channel1;
	}
	
	public static SSLParser createSslSvrParser() throws GeneralSecurityException, IOException {
		MockSSLEngineFactory sslFactory = new MockSSLEngineFactory();
		BufferPool pool = new BufferCreationPool(false, 17000, 1000);
		SSLEngine svrSsl = sslFactory.createEngineForServerSocket();
		return AsyncSSLFactory.create("svr", svrSsl, pool);
	}
}
