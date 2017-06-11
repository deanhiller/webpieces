package org.webpieces.nio.api.integ;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.asyncserver.api.AsyncConfig;
import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

public class IntegTestEchoClientToOurServer {

	//private static final Logger log = LoggerFactory.getLogger(IntegTestEchoClientToOurServer.class);
	
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
		new IntegTestEchoClientToOurServer().testSoTimeoutOnSocket();
	}
	
	public void testSoTimeoutOnSocket() throws InterruptedException {
		EchoClient client = new EchoClient();
		
		Executor executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("serverThread"));
		BufferPool pool = new BufferCreationPool();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("server", pool, new BackpressureConfig(), executor);
		AsyncServerManager serverMgr = AsyncServerMgrFactory.createAsyncServer(mgr);
		AsyncServer server = serverMgr.createTcpServer(new AsyncConfig("tcpServer"), new IntegTestLocalhostServerListener());
		server.start(new InetSocketAddress(8080));

		client.start(8080);
		
		synchronized(this) {
			this.wait();
		}
	}
	
}
