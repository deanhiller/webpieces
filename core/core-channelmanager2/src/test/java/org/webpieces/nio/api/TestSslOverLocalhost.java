package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.data.api.TwoPools;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.mocks.MockConnectionListener;
import org.webpieces.nio.api.mocks.MockMulithreadedSslDataListener;
import org.webpieces.util.threading.NamedThreadFactory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestSslOverLocalhost {

	private MockMulithreadedSslDataListener mockSvrDataListener = new MockMulithreadedSslDataListener();
	private MockConnectionListener mockConnListener = new MockConnectionListener(mockSvrDataListener);
	private MockMulithreadedSslDataListener mockClientDataListener = new MockMulithreadedSslDataListener();
	
	@Test
	public void testBasic() throws InterruptedException, ExecutionException, TimeoutException {
		ChannelManager svrMgr = createSvrChanMgr("server");
		SelfSignedSSLEngineFactory sslFactory = new SelfSignedSSLEngineFactory();
		TCPServerChannel svrChannel = svrMgr.createTCPServerChannel("svrChan", mockConnListener, sslFactory);
		svrChannel.bind(new InetSocketAddress(8444));
		
		int port = svrChannel.getLocalAddress().getPort();
		
		//don't really need to use a separate chan mgr but we will here..
		ChannelManager chanMgr = createSvrChanMgr("client");
		TCPChannel channel = chanMgr.createTCPChannel("client", sslFactory.createEngineForClient("cvs.xsoftware.biz", port));
		CompletableFuture<Void> future = channel.connect(new InetSocketAddress("localhost", port), mockClientDataListener);
		future.get();

		byte[] data = new byte[] {0, 2, 4, 6, 8, 10};
		ByteBuffer buf = ByteBuffer.wrap(data);
		channel.write(buf);

		ByteBuffer result = mockSvrDataListener.getFirstBuffer().get(2, TimeUnit.SECONDS);
		
		byte[] newData = new byte[result.remaining()];
		result.get(newData);
		
		Assert.assertEquals(data.length, newData.length);
		for(int i = 0; i < data.length; i++) {
			Assert.assertEquals(data[i], newData[i]);
		}
		
		//verify sniServerName was used in looking up security cert.
		String host = sslFactory.getCachedHost();
		Assert.assertEquals("cvs.xsoftware.biz", host);
	}

	private ChannelManager createSvrChanMgr(String name) {
		ExecutorService executor = Executors.newFixedThreadPool(10, new NamedThreadFactory(name));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(Metrics.globalRegistry);
		ChannelManager svrMgr = factory.createMultiThreadedChanMgr(name+"Mgr", new TwoPools("pl", new SimpleMeterRegistry()), new BackpressureConfig(), executor);
		return svrMgr;
	}
}
