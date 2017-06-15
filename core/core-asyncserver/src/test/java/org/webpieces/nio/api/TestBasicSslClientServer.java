package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class TestBasicSslClientServer {

	private static final Logger log = LoggerFactory.getLogger(TestBasicSslClientServer.class);
	private BufferCreationPool pool;
	private List<Integer> values = new ArrayList<>();

	@Test
	public void testBasic() throws InterruptedException {
		pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createSingleThreadedChanMgr("sslChanMgr", pool, new BackpressureConfig());
		AsyncServerManager svrFactory = AsyncServerMgrFactory.createAsyncServer(mgr);
		
		SSLEngineFactoryForTest f = new SSLEngineFactoryForTest();
		InetSocketAddress addr = new InetSocketAddress("localhost", 0);
		AsyncServer svr = svrFactory.createTcpServer(new AsyncConfig("sslTcpSvr"), new SvrDataListener(), f);
		svr.start(addr);

		InetSocketAddress bound = svr.getUnderlyingChannel().getLocalAddress();
		System.out.println("port="+bound.getPort());
		
		TCPChannel channel = mgr.createTCPChannel("client", f.createEngineForSocket());
		CompletableFuture<Void> connect = channel.connect(bound, new ClientListener());
		connect.thenAccept(v -> writeData(channel));
		
		synchronized (pool) {
			while(values.size() < 10) 
				pool.wait();
		}
		
		for(int i = 0; i < values.size(); i++) {
			Assert.assertEquals(new Integer(i), values.get(i));
		}
	}
	
	private void writeData(Channel c) {
		for(int i = 0; i < 10; i++) {
			ByteBuffer buffer = pool.nextBuffer(2);
			buffer.put((byte) i);
			buffer.flip();
			c.write(buffer);
		}
	}

	private class ClientListener implements DataListener {

		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			int value = b.get();
			log.info("incoming client data="+value);
			pool.releaseBuffer(b);
			
			values.add(value);
			if(values.size() >= 10) {
				synchronized (pool) {
					pool.notifyAll();
				}
			}
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void farEndClosed(Channel channel) {
			log.info("server closed");
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.error("client failed", e);
		}
	}
	
	private class SvrDataListener implements AsyncDataListener {

		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			log.info("server received data");
			return channel.write(b)
					.thenApply(c -> null);
		}

		@Override
		public void connectionOpened(TCPChannel channel, boolean isReadyForWrites) {
			log.info("opened channel="+channel);
		}
		
		@Override
		public void farEndClosed(Channel channel) {
			log.info("svr side....client must have closed the channel="+channel);
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.info("failed", e);
		}
	}
}
