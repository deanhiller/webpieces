package org.webpieces.webserver.impl;

import javax.inject.Singleton;

import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.nio.api.SSLEngineFactory;
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
		if(config.getSSLEngineFactory() == null) 
			binder.bind(SSLEngineFactory.class).toProvider(Providers.of(null));
		else 
			binder.bind(SSLEngineFactory.class).toInstance(config.getSSLEngineFactory());
		
		binder.bind(WebServer.class).to(WebServerImpl.class);

		binder.bind(WebServerConfig.class).toInstance(config);
	}

	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(WebServerConfig config) {
		return HttpFrontendFactory.createFrontEnd("httpFrontEnd", config.getNumFrontendServerThreads());
	}
	
}
