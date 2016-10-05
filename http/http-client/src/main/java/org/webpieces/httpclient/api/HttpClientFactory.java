package org.webpieces.httpclient.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
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
		HttpParser httpParser = HttpParserFactory.createParser(pool);
		Http2Parser http2Parser = Http2ParserFactory.createParser(pool);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		return createHttpsClient(mgr, httpParser, http2Parser, sslFactory);
	}
	
	public static HttpClient createHttpClient(int numThreads) {
		return createHttpsClient(numThreads, null);
	}
	
	public static HttpClient createHttpClient(ChannelManager mgr, HttpParser httpParser, Http2Parser http2Parser) {
		return createHttpsClient(mgr, httpParser, http2Parser, null);
	}

	public static HttpClient createHttpsClient(ChannelManager mgr, HttpParser httpParser, Http2Parser http2Parser, HttpsSslEngineFactory factory) {
		if(factory != null)
			return new HttpsClientImpl(mgr, httpParser, http2Parser, factory);
		else
			return new HttpClientImpl(mgr, httpParser, http2Parser);
	}
}
