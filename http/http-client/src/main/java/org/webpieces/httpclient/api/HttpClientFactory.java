package org.webpieces.httpclient.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
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
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		return createHttpsClient(mgr, pool, sslFactory);
	}
	
	public static HttpClient createHttpClient(int numThreads) {
		return createHttpsClient(numThreads, null);
	}
	
	public static HttpClient createHttpClient(ChannelManager mgr, BufferPool bufferPool) {
		return createHttpsClient(mgr, bufferPool, null);
	}

	public static HttpClient createHttpsClient(ChannelManager mgr, BufferPool bufferPool, HttpsSslEngineFactory factory) {
		if(factory != null)
			return new HttpsClientImpl(mgr, bufferPool, factory);
		else
			return new HttpClientImpl(mgr, bufferPool);
	}
}
