package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public class TestBasicSslClientServer {

	private static final Logger log = LoggerFactory.getLogger(TestBasicSslClientServer.class);
	private BufferCreationPool pool;
	private List<Integer> values = new ArrayList<>();

	@Test
	public void testBasic() throws InterruptedException {
		pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createSingleThreadedChanMgr("sslChanMgr", pool);
		AsyncServerManager svrFactory = AsyncServerMgrFactory.createAsyncServer(mgr);
		
		SSLEngineFactoryForTest f = new SSLEngineFactoryForTest();
		InetSocketAddress addr = new InetSocketAddress("localhost", 0);
		AsyncServer svr = svrFactory.createTcpServer(new AsyncConfig("sslTcpSvr", addr), new SvrDataListener(), f);
		
		InetSocketAddress bound = svr.getUnderlyingChannel().getLocalAddress();
		System.out.println("port="+bound.getPort());
		
		TCPChannel channel = mgr.createTCPChannel("client", f.createEngineForSocket());
		CompletableFuture<Channel> connect = channel.connect(bound, new ClientListener());
		connect.thenAccept(c -> writeData(c));
		
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
		public void incomingData(Channel channel, ByteBuffer b) {
			int value = b.get();
			log.info("incoming client data="+value);
			pool.releaseBuffer(b);
			
			values.add(value);
			if(values.size() >= 10) {
				synchronized (pool) {
					pool.notifyAll();
				}
			}
		}

		@Override
		public void farEndClosed(Channel channel) {
			log.info("server closed");
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.error("client failed", e);
		}

		@Override
		public void applyBackPressure(Channel channel) {
			log.info("apply backpressure");
		}

		@Override
		public void releaseBackPressure(Channel channel) {
			log.info("releasebackpressure");
		}
		
	}
	
	private class SvrDataListener implements DataListener {

		@Override
		public void incomingData(Channel channel, ByteBuffer b) {
			if(!b.hasRemaining())
				return;
			log.info("server received data");
			channel.write(b);
		}

		@Override
		public void farEndClosed(Channel channel) {
			log.info("svr side....client must have closed the channel");
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.info("failed", e);
		}

		@Override
		public void applyBackPressure(Channel channel) {
			log.info("svr apply backpressure");
		}

		@Override
		public void releaseBackPressure(Channel channel) {
			log.info("svr releasebackpressure");
		}
	}
}
