package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.data.api.BufferCreationPool;

public class TestBasicSsl {

	private MockDataListener mockSvrDataListener = new MockDataListener();
	private MockConnectionListener mockConnListener = new MockConnectionListener(mockSvrDataListener);
	private MockDataListener mockClientDataListener = new MockDataListener();
	
	@Test
	public void testBasic() throws InterruptedException, ExecutionException {
		ChannelManager svrMgr = createSvrChanMgr("server");
		TestSSLEngineFactory sslFactory = new TestSSLEngineFactory();
		TCPServerChannel svrChannel = svrMgr.createTCPServerChannel("svrChan", mockConnListener, sslFactory);
		svrChannel.bind(new InetSocketAddress(8889));
		
		//don't really need to use a separate chan mgr but we will here..
		ChannelManager chanMgr = createSvrChanMgr("client");
		TCPChannel channel = chanMgr.createTCPChannel("client", sslFactory.createEngineForClient());
		CompletableFuture<Channel> future = channel.connect(new InetSocketAddress("localhost", 8889), mockClientDataListener);
		future.get();

		byte[] data = new byte[] {0, 2, 4, 6, 8, 10};
		ByteBuffer buf = ByteBuffer.wrap(data);
		channel.write(buf);

		ByteBuffer result = mockSvrDataListener.getFirstBuffer().get();
		
		byte[] newData = new byte[result.remaining()];
		result.get(newData);
		
		Assert.assertEquals(data.length, newData.length);
		for(int i = 0; i < data.length; i++) {
			Assert.assertEquals(data[i], newData[i]);
		}
	}

	private ChannelManager createSvrChanMgr(String name) {
		ExecutorService executor = Executors.newFixedThreadPool(10, new NamedThreadFactory(name));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager svrMgr = factory.createMultiThreadedChanMgr(name+"Mgr", new BufferCreationPool(), executor);
		return svrMgr;
	}
}
