package org.webpieces.nio.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.mocks.MockAsyncListener;
import org.webpieces.nio.api.mocks.MockAsyncListener.ConnectionOpen;
import org.webpieces.nio.api.mocks.MockJdk;
import org.webpieces.nio.api.mocks.MockSvrChannel;
import org.webpieces.nio.api.mocks.MockSvrSideJdkChannel;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.SSLParser;
import org.webpieces.ssl.api.dto.SslAction;
import org.webpieces.ssl.api.dto.SslActionEnum;
import org.webpieces.util.threading.DirectExecutor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

public class TestSslBasicSvr {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
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

		SSLEngineFactoryForTest sslFactory = new SSLEngineFactoryForTest();	

		MeterRegistry meters = Metrics.globalRegistry;
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(mockJdk, meters);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("test'n", new BufferCreationPool(), new BackpressureConfig(), new DirectExecutor());

		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(mgr, meters);
		server = svrMgr.createTcpServer(new AsyncConfig(), listener, sslFactory);
		
		CompletableFuture<Void> future = server.start(new InetSocketAddress(8443));
		Assert.assertFalse(future.isDone());
		
		mockJdk.setThread(Thread.currentThread());
		mockJdk.fireSelector();
		future.get(2, TimeUnit.SECONDS);
		
		BufferPool pool = new BufferCreationPool(false, 17000, 1000);
		SSLEngine clientSsl = sslFactory.createEngineForSocket();
		clientSslParser = AsyncSSLFactory.create("svr", clientSsl, pool);

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
	}

	@After
	public void teardown() {
		System.clearProperty("jdk.tls.server.protocols");
		System.clearProperty("jdk.tls.client.protocols");
	}

	//begin handshake results in ONE packet client -> server (server creates runnable, creating ONE
	//server creates runnable, runs it creating ONE packet server -> client
	//client creates runnable, runs it creating THREE packets client -> server
	//all 3 received, server creates TWO packets  client -> server (server is connected here)
	//client receives two packets and is connected
	
	@Test
	public void testBasic() throws InterruptedException, ExecutionException, TimeoutException, GeneralSecurityException, IOException {
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
	public void testSplitData() throws InterruptedException, ExecutionException, TimeoutException {
		SslAction action = parseIncoming(); //3 encrypted packets sent here
		Assert.assertEquals(SslActionEnum.SEND_TO_SOCKET, action.getSslAction());
		
		List<DataWrapper> split = split(action.getEncryptedData());
		
		DataWrapper back = dataGen.chainDataWrappers(split.get(0), split.get(1), split.get(2), split.get(3));
		
		byte[] b1 = action.getEncryptedData().createByteArray();
		byte[] b2 = back.createByteArray();
		Assert.assertEquals(b1.length, b2.length);
		for(int i = 0; i < b1.length; i++) {
			Assert.assertEquals(b1[i], b2[i]);
		}
		
		mockChannel.forceDataRead(mockJdk, split.get(0));
		Assert.assertEquals(0, mockChannel.getPayloadQueueSize());
		
		mockChannel.forceDataRead(mockJdk, split.get(1));
		Assert.assertEquals(0, mockChannel.getPayloadQueueSize());
		
		mockChannel.forceDataRead(mockJdk, split.get(2));
		Assert.assertEquals(0, mockChannel.getPayloadQueueSize());

		mockChannel.forceDataRead(mockJdk, split.get(3));
		Assert.assertEquals(2, mockChannel.getPayloadQueueSize());

		Assert.assertEquals(SslActionEnum.WAIT_FOR_MORE_DATA_FROM_REMOTE_END, parseIncoming().getSslAction());
		Assert.assertEquals(SslActionEnum.SEND_LINK_ESTABLISHED_TO_APP, parseIncoming().getSslAction());

		ConnectionOpen openedInfo = listener.getConnectionOpenedInfo();
		Assert.assertEquals(true, openedInfo.isReadyForWrites);
		Assert.assertEquals(channel, openedInfo.channel);
		
		transferBigData();
	}
	
	private List<DataWrapper> split(DataWrapper encryptedData) {
		List<? extends DataWrapper> split = dataGen.split(encryptedData, encryptedData.getReadableSize()/2);
		
		List<DataWrapper> all = new ArrayList<>();
		List<? extends DataWrapper> split1 = dataGen.split(split.get(0), split.get(0).getReadableSize()/2);
		List<? extends DataWrapper> split2 = dataGen.split(split.get(1), split.get(1).getReadableSize()/2);
		all.addAll(split1);
		all.addAll(split2);
		return all;
	}

	private SslAction parseIncoming() throws InterruptedException, ExecutionException, TimeoutException {
		DataWrapper payload = mockChannel.nextPayload();
		CompletableFuture<List<SslAction>> resultFuture2 = clientSslParser.parseIncoming(payload);
		List<SslAction> result2 = resultFuture2.get(2, TimeUnit.SECONDS);
		Assert.assertEquals(1, result2.size());
		return result2.get(0);
	}
	
}
