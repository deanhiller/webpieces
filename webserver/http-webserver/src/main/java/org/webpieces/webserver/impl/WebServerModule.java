package org.webpieces.webserver.impl;

import java.util.concurrent.*;

import javax.inject.Singleton;

import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.frontend2.api.HttpFrontendManager;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.*;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.metrics.MetricsCreator;
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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class WebServerModule implements Module {
	
	private final WebServerConfig config;
	private final WebServerPortInformation portLookup;
	private final MaxRequestConfig maxRequestConfig;
	private boolean hasCoreModule;

	public WebServerModule(WebServerConfig config, WebServerPortInformation portLookup, boolean hasCoreModule) {
		this.config = config;
		this.portLookup = portLookup;
		this.hasCoreModule = hasCoreModule;
		this.maxRequestConfig = config.getBackpressureConfig().getMaxRequestConfig();
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(WebServer.class).to(WebServerImpl.class);

		binder.bind(WebServerConfig.class).toInstance(config);

		binder.bind(BackpressureConfig.class).toInstance(config.getBackpressureConfig());

		binder.bind(RouterLookup.class).to(RouterLookupProxy.class).asEagerSingleton();
		
		binder.bind(ConverterLookup.class).to(ConverterLookupProxy.class).asEagerSingleton();

		binder.bind(Time.class).to(TimeImpl.class).asEagerSingleton();

		binder.bind(TemplateApi.class).to(WebServerTemplateProxy.class);

		//what the webserver writes to
		binder.bind(WebServerPortInformation.class).toInstance(portLookup);

		if(!hasCoreModule)
			binder.bind(MeterRegistry.class).to(SimpleMeterRegistry.class);
	}
	
	@Provides
	@Singleton
	public ScheduledExecutorService provideTimer() {
		return new ScheduledThreadPoolExecutor(10, new NamedThreadFactory("webpieces-timer"));
	}
	
	@Provides 
	@Singleton
	public Executor providesExecutor(WebServerConfig config, MeterRegistry metrics) {
		String id = config.getId()+".tPool";
		Executor executor = Executors.newFixedThreadPool(config.getNumFrontendServerThreads(), new NamedThreadFactory(id));
		MetricsCreator.monitor(metrics, executor, id);
		return executor;
	}
	
	@Provides
	@Singleton
	public ChannelManager providesChanMgr(WebServerConfig config, Executor executor, BufferPool pool, MeterRegistry metrics) {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(metrics);
		ChannelManager chanMgr = factory.createMultiThreadedChanMgr(config.getId(), pool, config.getBackpressureConfig(), executor);
		
		return chanMgr;
	}

	@Provides
	@Singleton
	public Throttle providesThrottle(ChannelManager cm) {
		return cm.getThrottle();
	}

	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(
			ChannelManager chanMgr, 
			ScheduledExecutorService timer, 
			BufferPool pool,
			Time time, 
			WebServerConfig config,
			MeterRegistry metrics
	) {		
		HttpParser httpParser = HttpParserFactory.createParser("a", metrics, pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		InjectionConfig injConfig = new InjectionConfig(http2Parser, time, config.getHttp2Config());

		return HttpFrontendFactory.createFrontEnd(chanMgr, timer, injConfig, httpParser, metrics);
	}
	
}
