package org.webpieces.frontend2.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.impl.FrontEndServerManagerImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;
import com.webpieces.util.time.TimeImpl;

public abstract class HttpFrontendFactory {
	
	public static final String HTTP2_ENGINE_THREAD_POOL = "http2EngineThreadPool";
	public static final String FILE_READ_EXECUTOR = "fileReadExecutor";
	
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
		
		HttpParser httpParser = HttpParserFactory.createParser(pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		
		InjectionConfig injConfig = new InjectionConfig(http2Parser, new TimeImpl(), new Http2Config());
		Http2ServerEngineFactory svrEngineFactory = new Http2ServerEngineFactory(injConfig );
		
		return new FrontEndServerManagerImpl(svrMgr, timer, svrEngineFactory, httpParser);
	}
	
	public static HttpFrontendManager createFrontEnd(
			ChannelManager chanMgr, ScheduledExecutorService timer, InjectionConfig injConfig) {
        BufferCreationPool pool = new BufferCreationPool();
		HttpParser httpParser = HttpParserFactory.createParser(pool);
		return createFrontEnd(chanMgr, timer, injConfig, httpParser);
	}
	
	public static HttpFrontendManager createFrontEnd(
			ChannelManager chanMgr, ScheduledExecutorService timer, InjectionConfig injConfig, HttpParser parsing) {
		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(chanMgr);
		Http2ServerEngineFactory svrEngineFactory = new Http2ServerEngineFactory(injConfig );
		return new FrontEndServerManagerImpl(svrMgr, timer, svrEngineFactory, parsing);
	}
	
}
