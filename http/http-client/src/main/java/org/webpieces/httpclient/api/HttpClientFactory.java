package org.webpieces.httpclient.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.httpclient.impl.HttpClientImpl;
import org.webpieces.httpclient.impl.HttpsClientImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

public abstract class HttpClientFactory {

	public static HttpClient createHttpsClient(int numThreads, HttpsSslEngineFactory sslFactory) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		BufferCreationPool pool = new BufferCreationPool();
		HttpParser parser = HttpParserFactory.createParser(pool);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		return createHttpsClient(mgr, parser, sslFactory);		
	}
	
	public static HttpClient createHttpClient(int numThreads) {
		return createHttpsClient(numThreads, null);
	}
	
	public static HttpClient createHttpClient(ChannelManager mgr, HttpParser parser) {
		return createHttpsClient(mgr, parser, null);
	}

	public static HttpClient createHttpsClient(ChannelManager mgr, HttpParser parser, HttpsSslEngineFactory factory) {
		if(factory != null)
			return new HttpsClientImpl(mgr, parser, factory);
		else
			return new HttpClientImpl(mgr, parser);
	}
}
