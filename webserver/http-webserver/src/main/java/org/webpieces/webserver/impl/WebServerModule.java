package org.webpieces.webserver.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Singleton;

import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Providers;

public class WebServerModule implements Module {

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
	}

//  Firefox keeps connecting pre-emptively with no requests for seconds (maybe so it is ready to just send one when needed)	
//	@Provides
//	@Singleton
//	public ScheduledExecutorService provideTimer() {
//		return new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("webpieces-timer"));
//	}
	
	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(WebServerConfig config) {
		return HttpFrontendFactory.createFrontEnd("httpFrontEnd", config.getNumFrontendServerThreads(), null);
	}
	
}
