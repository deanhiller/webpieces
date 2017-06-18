package org.webpieces.throughput.server;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.frontend2.api.FrontendConfig;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class ServerAsync {
	private static final Logger log = LoggerFactory.getLogger(ServerAsync.class);

	private boolean multiThreaded;

	public ServerAsync(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}

	public CompletableFuture<InetSocketAddress> start() {
    	log.error("running ASYNC HTTP1.1 AND HTTP2 SERVER");

		HttpServer server = createFrontend(multiThreaded);
		CompletableFuture<Void> future = server.start();
		return future.thenApply(v -> server.getUnderlyingChannel().getLocalAddress());
	}

	private HttpServer createFrontend(boolean multiThreaded) {
		if(multiThreaded)
			return createFrontendMultiThreaded();
		
		BufferCreationPool pool = new BufferCreationPool();
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createSingleThreadedChanMgr("svrCmLoop", pool, new BackpressureConfig());

		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(chanMgr);

		HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd(svrMgr, pool, new BackpressureConfig());
		return mgr.createHttpServer(new FrontendConfig("asyncsvr"), new EchoListener());
	}
	
	private HttpServer createFrontendMultiThreaded() {
		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
		HttpFrontendManager mgr = HttpFrontendFactory.createFrontEnd("deansvr", 20, timer, new BufferCreationPool(), new BackpressureConfig());
		
		return mgr.createHttpServer(new FrontendConfig("asyncsvr"), new EchoListener());
	}
}
