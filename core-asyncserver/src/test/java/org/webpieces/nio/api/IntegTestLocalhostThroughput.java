package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.BufferPool;

public class IntegTestLocalhostThroughput {

	private static final Logger log = LoggerFactory.getLogger(IntegTestLocalhostThroughput.class);
	private BytesRecorder recorder = new BytesRecorder();
	
	/**
	 * Here, we will simulate a bad hacker client that sets his side so_timeout to infinite
	 * and then refuses to read response data back in but keeps writing into our server to
	 * crash the server as it backs up on responses....ie. we keep receiving requests and holding
	 * on to them so memory keeps growing and growing or our write queue keeps growing unbounded
	 * 
	 * so this test ensures we fix that scenario
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		new IntegTestLocalhostThroughput().testSoTimeoutOnSocket();
	}
	
	public void testSoTimeoutOnSocket() throws InterruptedException {
		Executor executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("serverThread"));
		BufferPool pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("server", pool, executor);
		AsyncServerManager server = AsyncServerMgrFactory.createAsyncServer(mgr);
		server.createTcpServer("tcpServer", new InetSocketAddress(8080), new IntegTestLocalhostServerListener());
		
		BufferPool pool2 = new BufferCreationPool();
		DataListener listener = new ClientDataListener(pool2, recorder);
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		TCPChannel channel = createClientChannel(pool2, executor2);
		//TCPChannel channel = createNettyChannel();

		recorder.start();

		CompletableFuture<Channel> connect = channel.connect(new InetSocketAddress(8080), listener);
		connect.thenAccept(p -> runWriting(channel));
		
		synchronized(this) {
			this.wait();
		}
	}

	private TCPChannel createClientChannel(BufferPool pool2, Executor executor) {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, executor);
		TCPChannel channel = mgr.createTCPChannel("clientChan");
		return channel;
	}

	private void runWriting(Channel channel) {
		log.info("starting writing");
		write(channel, null);
	}

	private void write(Channel channel, String reason) {
		byte[] data = new byte[10240];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		CompletableFuture<Channel> write = channel.write(buffer);
		
		write
			.thenAccept(p -> write(channel, "wrote data from client"))
			.whenComplete((r, e) -> finished(r, e))
			.exceptionally(e -> {
				logIt(e);
				return null;
			});
	}

	private void logIt(Throwable e) {
		log.warn("failed to write", e);
	}

	private void finished(Void r, Throwable e) {
		if(e != null) 
			log.info("failed due to reason="+e.getMessage());
	}
	
}
