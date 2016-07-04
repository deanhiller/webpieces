package org.webpieces.httpproxy.impl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Singleton;

import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.api.ProxyConfig;
import org.webpieces.util.threading.NamedThreadFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

public class HttpProxyModule implements Module {

	private ProxyConfig config;

	public HttpProxyModule(ProxyConfig config) {
		this.config = config;
	}
	@Override
	public void configure(Binder binder) {
		binder.bind(HttpProxy.class).to(HttpProxyImpl.class);

		binder.bind(ProxyConfig.class).toInstance(config);
	}

	@Provides
	@Singleton
	public ScheduledExecutorService provideTimer() {
		return new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("webpieces-timer"));
	}
	
	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(ProxyConfig config, ScheduledExecutorService timer) {
		return HttpFrontendFactory.createFrontEnd("httpFrontEnd", config.getNumFrontendServerThreads(), timer);
	}
	
	@Provides
	@Singleton
	public HttpClient provideHttpClient(ProxyConfig config) {
		if(config.isForceAllConnectionToHttps())
			return HttpClientFactory.createHttpsClient(config.getNumHttpClientThreads(), new ForTestSslClientEngineFactory());
		
		return HttpClientFactory.createHttpClient(config.getNumHttpClientThreads());
	}
}
