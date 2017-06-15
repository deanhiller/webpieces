package org.webpieces.nio.api.integ;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class IntegTestClientNotRead {

	private static final Logger log = LoggerFactory.getLogger(IntegTestClientNotRead.class);
	private Timer timer = new Timer();
	private long timeMillis;
	private long totalBytes = 0;
	private Executor executor = Executors.newFixedThreadPool(1);

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
		new IntegTestClientNotRead().testSoTimeoutOnSocket();
	}
	
	public void testSoTimeoutOnSocket() throws InterruptedException {
		BufferCreationPool pool = new BufferCreationPool();
		AsyncServerManager serverMgr = AsyncServerMgrFactory.createAsyncServer("server", pool, new BackpressureConfig());
		AsyncServer server = serverMgr.createTcpServer(new AsyncConfig("tcpServer"), new IntegTestClientNotReadListener());
		server.start(new InetSocketAddress(8080));
		
		BufferCreationPool pool2 = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createSingleThreadedChanMgr("client", pool2, new BackpressureConfig());
		TCPChannel channel = mgr.createTCPChannel("clientChan");

		log.info("client");

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				logBytesTxfrd();
			}
		}, 1000, 5000);
		
		CompletableFuture<Void> connect = channel.connect(new InetSocketAddress(8080), new ClientDataListener());
		connect.thenAccept(p -> runWriting(channel));
		
		Thread.sleep(1000000000);
	}

	private void logBytesTxfrd() {
		long bytesTxfrd = getBytes();
		long totalTime = System.currentTimeMillis() - timeMillis;
		long bytesPerMs = bytesTxfrd / totalTime;
		log.info("time for bytes="+bytesTxfrd+". time="+totalTime+" rate="+bytesPerMs +" Bytes/Ms");
	}
	
	private void runWriting(Channel channel) {
		log.info("unregister for reads");

		timeMillis = System.currentTimeMillis();
		
		log.info("starting writing");
		write(channel, null, 0);
	}

	private synchronized long getBytes() {
		return totalBytes;
	}
	protected synchronized void recordBytes(int remaining) {
		totalBytes += remaining;
	}

	private void write(Channel channel, String reason, final int counter) {
		log.info("write from client. reason="+ reason);
		byte[] data = new byte[2000];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		CompletableFuture<Void> write = channel.write(buffer);
		final int count = counter+1;

		if(counter >= 100) {
			write
				.thenAccept(p -> write(channel, "wrote data from client", count))
				.whenComplete((r, e) -> finished(r, e));
		} else {
			write.thenAcceptAsync(p -> write(channel, "wrote data async", 0), executor)
				.whenComplete((r, e) -> finished(r, e));
		}
	}

	private void finished(Void r, Throwable e) {
		if(e != null) 
			log.info("failed due to reason="+e.getMessage(), e);
	}

	private class ClientDataListener implements DataListener {
		@Override
		public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
			recordBytes(b.remaining());
			
			if(b.remaining() != 2000)
				log.info("size of buffer="+b.remaining());	
			return CompletableFuture.completedFuture(null);
		}
		
		@Override
		public void farEndClosed(Channel channel) {
			log.info("far end closed");
		}
		
		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.info("failure", e);
		}
	}
}
