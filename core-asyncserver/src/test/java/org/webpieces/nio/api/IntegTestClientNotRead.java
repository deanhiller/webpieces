package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

public class IntegTestClientNotRead {

	private static final Logger log = LoggerFactory.getLogger(IntegTestClientNotRead.class);
	private Timer timer = new Timer();
	private long timeMillis;
	private long totalBytes = 0;
	
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
		BufferCreationPool pool = new BufferCreationPool(false, 2000);
		AsyncServerManager server = AsyncServerMgrFactory.createAsyncServer("server", pool);
		server.createTcpServer("tcpServer", new InetSocketAddress(8080), new IntegTestClientNotReadListener(pool));
		
		BufferCreationPool pool2 = new BufferCreationPool(false, 2000);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createChannelManager("client", pool2);
		TCPChannel channel = mgr.createTCPChannel("clientChan");
		
		log.info("client keep alive="+channel.getKeepAlive()+" timeout="+channel.getWriteTimeoutMs());

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				logBytesTxfrd();
			}
		}, 1000, 5000);
		
		
		CompletableFuture<Channel> connect = channel.connect(new InetSocketAddress(8080));
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
		log.info("register for reads");

		timeMillis = System.currentTimeMillis();
		
		log.info("starting writing");
		write(channel, null);
	}

	private synchronized long getBytes() {
		return totalBytes;
	}
	protected synchronized void recordBytes(int remaining) {
		totalBytes += remaining;
	}

	private void write(Channel channel, String reason) {
		log.info("write from client. reason="+ reason);
		byte[] data = new byte[2000];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		CompletableFuture<Channel> write = channel.write(buffer);
		
		write
			.thenAccept(p -> write(channel, "wrote data from client"))
			.whenComplete((r, e) -> finished(r, e));
	}

	private void finished(Void r, Throwable e) {
		if(e != null) 
			log.info("failed due to reason="+e.getMessage());
	}

}
