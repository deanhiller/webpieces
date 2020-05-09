package org.webpieces.frontend2.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.impl.FrontEndServerManagerImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.metrics.MetricsCreator;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.util.time.TimeImpl;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;

import io.micrometer.core.instrument.MeterRegistry;

public abstract class HttpFrontendFactory {
		
	public static HttpFrontendManager createFrontEnd(AsyncServerManager svrMgr, BufferPool pool, Http2Config http2Config, MeterRegistry metrics) {
		ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
	
		HttpParser httpParser = HttpParserFactory.createParser(svrMgr.getName(), metrics, pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		
		InjectionConfig injConfig = new InjectionConfig(http2Parser, new TimeImpl(), http2Config);
		Http2ServerEngineFactory svrEngineFactory = new Http2ServerEngineFactory(injConfig );
		
		return new FrontEndServerManagerImpl(svrMgr, timer, svrEngineFactory, httpParser);
	}
	
	/**
	 * 
	 * @param id Use for logging and also file recording names
	 * use the SessionExecutorImpl found in webpieces
	 * @param metrics 
	 * 
	 * @return
	 */
	public static HttpFrontendManager createFrontEnd(
			String id, ScheduledExecutorService timer, BufferPool pool, FrontendMgrConfig config, MeterRegistry metrics) {
		Executor executor = Executors.newFixedThreadPool(config.getThreadPoolSize(), new NamedThreadFactory(id));
		MetricsCreator.monitor(metrics, executor, id);

		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(metrics);
		ChannelManager chanMgr = factory.createMultiThreadedChanMgr(id, pool, config.getBackpressureConfig(), executor);

		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(chanMgr, metrics);
		
		HttpParser httpParser = HttpParserFactory.createParser(id, metrics, pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		
		InjectionConfig injConfig = new InjectionConfig(http2Parser, new TimeImpl(), config.getHttp2Config());
		Http2ServerEngineFactory svrEngineFactory = new Http2ServerEngineFactory(injConfig );
		
		return new FrontEndServerManagerImpl(svrMgr, timer, svrEngineFactory, httpParser);
	}
	
	public static HttpFrontendManager createFrontEnd(
			ChannelManager chanMgr, ScheduledExecutorService timer, InjectionConfig injConfig, MeterRegistry metrics) {
        TwoPools pool = new TwoPools(chanMgr.getName()+".bufpoolmain", metrics);
		HttpParser httpParser = HttpParserFactory.createParser(chanMgr.getName(), metrics, pool);
		return createFrontEnd(chanMgr, timer, injConfig, httpParser, metrics);
	}
	
	public static HttpFrontendManager createFrontEnd(
			ChannelManager chanMgr, ScheduledExecutorService timer, InjectionConfig injConfig, HttpParser parsing, MeterRegistry metrics) {
		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(chanMgr, metrics);
		Http2ServerEngineFactory svrEngineFactory = new Http2ServerEngineFactory(injConfig );
		return new FrontEndServerManagerImpl(svrMgr, timer, svrEngineFactory, parsing);
	}
	
}
