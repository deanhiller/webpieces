package org.webpieces.nio.api.throughput;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.threading.NamedThreadFactory;

import io.micrometer.core.instrument.Metrics;

public class IntegTestLocalhostThroughput {

	//private static final Logger log = LoggerFactory.getLogger(IntegTestLocalhostThroughput.class);
	private BytesRecorder recorder = new BytesRecorder();
	
	/**
	 * Here, we will simulate a bad hacker client that sets his side so_timeout to infinite
	 * and then refuses to read response data back in but keeps writing into our server to
	 * crash the server as it backs up on responses....ie. we keep receiving requests and holding
	 * on to them so memory keeps growing and growing or our write queue keeps growing unbounded
	 * 
	 * so this test ensures we fix that scenario
	 * @throws InterruptedException 
	 * @throws TimeoutException 
	 * @throws ExecutionException 
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		new IntegTestLocalhostThroughput().testThroughput();
	}
	
	public void testThroughput() throws InterruptedException, ExecutionException, TimeoutException {
		Executor executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("serverThread"));
		BufferPool pool = new BufferCreationPool(false, 32768, 100000);
		BackpressureConfig config = new BackpressureConfig();
		config.setMaxBytes(16_384*10);
		config.setStartReadingThreshold(512);
		ChannelManager mgr = createChanMgr("server", executor, pool, config);
		AsyncServerManager serverMgr = AsyncServerMgrFactory.createAsyncServer(mgr);
		AsyncServer server = serverMgr.createTcpServer(new AsyncConfig("tcpServer"), new AsyncServerDataListener(recorder, pool));
		server.start(new InetSocketAddress(8080));
		
		BufferPool pool2 = new BufferCreationPool(false, 32768, 100000);
		DataListener listener = new ClientDataListener(pool2, recorder);
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		TCPChannel channel = createClientChannel(pool2, executor2, config);

		recorder.setClientChannel(channel);
		recorder.start();

		CompletableFuture<Void> connect = channel.connect(new InetSocketAddress(8080), listener);
		connect.get(2, TimeUnit.SECONDS);
		
		synchronized(this) {
			this.wait();
		}
	}

	private ChannelManager createChanMgr(String id, Executor executor, BufferPool pool, BackpressureConfig config) {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(Metrics.globalRegistry);
		//This is more reasonable to compare with as introducing a threadpool is something that happens on all systems and slows things
		//down A TON due to context switching until you add back parsing, SSL handshaking and business logic at which point it becomes
		//more negligible.  (ie. just switching to createMultiThreaded here can tank throughput in half BUT that will not happen in 
		//a production app where the issue is in business logic code, parsing, ssl, etc).
		ChannelManager mgr = factory.createSingleThreadedChanMgr(id, pool, config);
		return mgr;
	}

	private TCPChannel createClientChannel(BufferPool pool2, Executor executor, BackpressureConfig config) {
		ChannelManager mgr = createChanMgr("client", executor, pool2, config);
		TCPChannel channel = mgr.createTCPChannel("clientChan");
		return channel;
	}

}
