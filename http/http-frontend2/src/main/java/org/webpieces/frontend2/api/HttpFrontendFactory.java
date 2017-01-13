package org.webpieces.frontend2.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.impl.FrontEndServerManagerImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;

public abstract class HttpFrontendFactory {
	
	/**
	 * 
	 * @param id Use for logging and also file recording names
	 * @param threadPoolSize The size of the threadpool, although all data comes in order as we
	 * use the SessionExecutorImpl found in webpieces
	 * 
	 * @return
	 */
	public static HttpFrontendManager createFrontEnd(String id, int threadPoolSize, ScheduledExecutorService timer, BufferPool pool) {
		Executor executor = Executors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory(id));
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createMultiThreadedChanMgr(id, pool, executor);

		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(chanMgr);
		
		return createFrontEnd(svrMgr, timer, pool);
	}
	
	public static HttpFrontendManager createFrontEnd(AsyncServerManager svrMgr, ScheduledExecutorService timer, BufferPool pool) {
		HttpParser httpParser = HttpParserFactory.createParser(pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		Http2ServerEngineFactory svrEngineFactory = new Http2ServerEngineFactory();
		ParsingLogic parsing = new ParsingLogic(httpParser, http2Parser, svrEngineFactory);

		return createFrontEnd(svrMgr, timer, pool, parsing);		
	}
	
	public static HttpFrontendManager createFrontEnd(
			AsyncServerManager svrManager, 
			ScheduledExecutorService svc, 
			BufferPool bufferPool, 
			ParsingLogic parsing 
	) {
		return new FrontEndServerManagerImpl(svrManager, svc, bufferPool, parsing);
	}
	
}
