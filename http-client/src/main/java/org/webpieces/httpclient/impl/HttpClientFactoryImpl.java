package org.webpieces.httpclient.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpclient.api.HttpsSslEngineFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;

public class HttpClientFactoryImpl extends HttpClientFactory {

	@Override
	public HttpClient createHttpsClient(int numThreads, HttpsSslEngineFactory sslFactory) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		BufferCreationPool pool = new BufferCreationPool();
		HttpParser parser = HttpParserFactory.createParser(pool);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		return createHttpsClient(mgr, parser, sslFactory);		
	}
	
	@Override
	public HttpClient createHttpClient(int numThreads) {
		return createHttpsClient(numThreads, null);
	}
	
	@Override
	public HttpClient createHttpClient(ChannelManager mgr, HttpParser parser) {
		return createHttpsClient(mgr, parser, null);
	}

	@Override
	public HttpClient createHttpsClient(ChannelManager mgr, HttpParser parser, HttpsSslEngineFactory factory) {
		if(factory != null)
			return new HttpsClientImpl(mgr, parser, factory);
		else
			return new HttpClientImpl(mgr, parser);
	}
}
