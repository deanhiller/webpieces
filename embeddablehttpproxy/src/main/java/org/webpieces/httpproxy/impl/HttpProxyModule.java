package org.webpieces.httpproxy.impl;

import javax.inject.Singleton;

import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpproxy.api.HttpFrontendFactory;
import org.webpieces.httpproxy.api.HttpFrontendManager;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.api.ProxyConfig;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.BufferPool;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;

public class HttpProxyModule implements Module {

	private ProxyConfig config;

	public HttpProxyModule(ProxyConfig config) {
		this.config = config;
	}
	@Override
	public void configure(Binder binder) {
		binder.bind(HttpProxy.class).to(HttpProxyImpl.class);

		binder.bind(ProxyConfig.class).toInstance(config);
		
		//TBD: probably delete and have HttpFrontend deliver an HttpSocket to be used with parser behind it
		//instead of an http channel (but how to make HttpSocket re-usable as this HttpSocket would be for
		//HttpResponses)
		
		BufferPool pool = new BufferCreationPool();
		binder.bind(HttpParser.class).toInstance(HttpParserFactory.createParser(pool));
	}

	@Provides
	@Singleton
	public HttpFrontendManager providesAsyncServerMgr(ProxyConfig config) {
		return HttpFrontendFactory.createFrontEnd("httpFrontEnd", config.getNumFrontendServerThreads());
	}
	
	@Provides
	@Singleton
	public HttpClient provideHttpClient(ProxyConfig config) {
		HttpClientFactory factory = HttpClientFactory.createFactory();
		if(config.isForceAllConnectionToHttps())
			return factory.createHttpsClient(config.getNumHttpClientThreads(), new ForTestSslClientEngineFactory());
		
		return factory.createHttpClient(config.getNumHttpClientThreads());
	}
}
