package org.webpieces.httpclient.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.httpclient.impl.HttpClientImpl;
import org.webpieces.httpclient.impl.HttpsClientImpl;
import org.webpieces.httpcommon.api.Http2SettingsMap;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;

public abstract class HttpClientFactory {

	public static HttpClient createHttpsClient(int numThreads, HttpsSslEngineFactory sslFactory) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		BufferCreationPool pool = new BufferCreationPool();
		HttpParser httpParser = HttpParserFactory.createParser(pool);
		HpackParser http2Parser = HpackParserFactory.createParser(pool, true);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		return createHttpsClient(mgr, httpParser, http2Parser, sslFactory, new Http2SettingsMap());
	}
	
	public static HttpClient createHttpClient(int numThreads) {
		return createHttpsClient(numThreads, null);
	}
	
	public static HttpClient createHttpClient(ChannelManager mgr, HttpParser httpParser, HpackParser http2Parser) {
		return createHttpsClient(mgr, httpParser, http2Parser, null, new Http2SettingsMap());
	}

	public static HttpClient createHttpClient(ChannelManager mgr, HttpParser httpParser, HpackParser http2Parser, Http2SettingsMap http2SettingsMap) {
		return createHttpsClient(mgr, httpParser, http2Parser, null, http2SettingsMap);
	}

	public static HttpClient createHttpsClient(ChannelManager mgr,
																							HttpParser httpParser,
																							HpackParser http2Parser,
																							HttpsSslEngineFactory factory,
																							Http2SettingsMap http2SettingsMap) {
		if(factory != null)
			return new HttpsClientImpl(mgr, httpParser, http2Parser, factory, http2SettingsMap);
		else
			return new HttpClientImpl(mgr, httpParser, http2Parser, http2SettingsMap);
	}
}
