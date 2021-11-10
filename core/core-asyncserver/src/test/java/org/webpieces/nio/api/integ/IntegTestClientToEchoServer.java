package org.webpieces.nio.api.integ;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.TwoPools;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.throughput.BytesRecorder;
import org.webpieces.nio.api.throughput.ClientDataListener;
import org.webpieces.nio.api.throughput.IntegTestLocalhostThroughput;
import org.webpieces.util.threading.NamedThreadFactory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class IntegTestClientToEchoServer {

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
		log.info("STARTING");
		new IntegTestClientToEchoServer().testSoTimeoutOnSocket();
	}
	
	public void testSoTimeoutOnSocket() throws InterruptedException {
		runEchoServer();
		
		BufferPool pool2 = new TwoPools("pl", new SimpleMeterRegistry());
		DataListener listener = new ClientDataListener(pool2, recorder);
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		TCPChannel channel = createClientChannel(pool2, executor2);
		//TCPChannel channel = createNettyChannel();

		recorder.start();

		XFuture<Void> connect = channel.connect(new InetSocketAddress(4444), listener);
		connect.thenAccept(p -> runWriting(channel));
		
		synchronized(this) {
			this.wait();
		}
	}

	private void runEchoServer() throws InterruptedException {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				new EchoServer().start();
			}
		};
		Thread t= new Thread(r);
		t.start();
		
		log.info("started echo server");
		Thread.sleep(2000);
		log.info("starting client");
	}
	
	private TCPChannel createClientChannel(BufferPool pool2, Executor executor) {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(Metrics.globalRegistry);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, new BackpressureConfig(), executor);
		TCPChannel channel = mgr.createTCPChannel("clientChan");
		return channel;
	}
	
	private void runWriting(Channel channel) {
		log.info("starting writing");
		write(channel, null);
	}

	private void write(Channel channel, String reason) {
//		counter++;
//		if(counter % 100000 == 0)
//			log.info("counter="+counter);
		byte[] data = new byte[10240];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		XFuture<Void> write = channel.write(buffer);
		
		write
			.thenAccept(p -> write(channel, "wrote data from client"))
			.whenComplete((r, e) -> finished(r, e))
			.exceptionally(e -> {
				logIt(e);
				return null;
			});
	}

	private void logIt(Throwable e) {
		log.error("failed to write", e);
	}

	private void finished(Void r, Throwable e) {
		if(e != null) 
			log.info("failed due to reason="+e.getMessage());
	}
}
