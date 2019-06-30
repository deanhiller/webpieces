package org.webpieces.webserver.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Named;
import javax.inject.Singleton;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.util.time.Time;
import org.webpieces.util.time.TimeImpl;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.client.InjectionConfig;

public class WebServerModule implements Module {

	private WebServerConfig config;

	public WebServerModule(WebServerConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(WebServer.class).to(WebServerImpl.class);

		binder.bind(WebServerConfig.class).toInstance(config);
		
		binder.bind(RouterLookup.class).to(RouterLookupProxy.class).asEagerSingleton();
		
		binder.bind(ConverterLookup.class).to(ConverterLookupProxy.class).asEagerSingleton();
		
		binder.bind(BufferPool.class).to(BufferCreationPool.class).asEagerSingleton();
		
		binder.bind(Time.class).to(TimeImpl.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	@Named(HttpFrontendFactory.FILE_READ_EXECUTOR)
	public ExecutorService provideExecutor() {
		return Executors.newFixedThreadPool(10, new NamedThreadFactory("fileReadCallbacks"));
	}
	
	@Provides
	@Singleton
	public ScheduledExecutorService provideTimer() {
		return new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("webpieces-timer"));
	}
	
	@Provides
	@Singleton
	@Named(HttpFrontendFactory.HTTP2_ENGINE_THREAD_POOL)
	public Executor providesEngineThreadPool(WebServerConfig config) {
		return Executors.newFixedThreadPool(config.getHttp2EngineThreadCount(), new NamedThreadFactory("http2Engine"));
	}
	
	@Provides
	@Singleton
	public ChannelManager providesChanMgr(WebServerConfig config, BufferPool pool) {
		String id = "webpiecesThreadPool";
		Executor executor = Executors.newFixedThreadPool(config.getNumFrontendServerThreads(), new NamedThreadFactory(id));
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createMultiThreadedChanMgr("selectorThread", pool, config.getBackpressureConfig(), executor);
		
		return chanMgr;
	}
	
	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(
			ChannelManager chanMgr, 
			ScheduledExecutorService timer, 
			@Named(HttpFrontendFactory.HTTP2_ENGINE_THREAD_POOL) Executor executor1, 
			BufferPool pool,
			Time time, 
			WebServerConfig config) {
		
		HttpParser httpParser = HttpParserFactory.createParser(pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		InjectionConfig injConfig = new InjectionConfig(http2Parser, time, config.getHttp2Config());

		return HttpFrontendFactory.createFrontEnd(chanMgr, timer, injConfig, httpParser);
	}
	
}
