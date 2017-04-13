package org.webpieces.webserver.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Named;
import javax.inject.Singleton;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Providers;

public class WebServerModule implements Module {

	public static final String FILE_READ_EXECUTOR = "fileReadExecutor";
	private WebServerConfig config;

	public WebServerModule(WebServerConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		if(config.getSslEngineFactory() == null) 
			binder.bind(SSLEngineFactory.class).toProvider(Providers.of(null));
		else 
			binder.bind(SSLEngineFactory.class).toInstance(config.getSslEngineFactory());
		
		binder.bind(WebServer.class).to(WebServerImpl.class);

		binder.bind(WebServerConfig.class).toInstance(config);
		
		binder.bind(RouterLookup.class).to(RouterLookupProxy.class).asEagerSingleton();
		
		binder.bind(ConverterLookup.class).to(ConverterLookupProxy.class).asEagerSingleton();
	}

	@Provides
	@Singleton
	@Named(FILE_READ_EXECUTOR)
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
	public BufferPool createBufferPool() {
		BufferCreationPool pool = new BufferCreationPool();
		return pool; 
	}
	
	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(WebServerConfig config, BufferPool pool, ScheduledExecutorService timer) {
		return HttpFrontendFactory.createFrontEnd("httpFrontEnd", config.getNumFrontendServerThreads(), timer, pool);
	}
	
}
