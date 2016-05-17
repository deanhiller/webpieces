package org.webpieces.httpclient.api;

import org.webpieces.httpclient.impl.HttpClientFactoryImpl;
import org.webpieces.nio.api.ChannelManager;

import com.webpieces.httpparser.api.HttpParser;

public abstract class HttpClientFactory {

	public static HttpClientFactory createFactory() {
		return new HttpClientFactoryImpl();
	}

	public abstract HttpClient createHttpClient(int numThreads);
	
	public abstract HttpClient createHttpsClient(int numThreads, HttpsSslEngineFactory sslFactory);
	
	/**
	 * BIG NOTE: You should pass the same BufferPool into both HttpParser and
	 * ChannelManager such that they create and release BufferPools to each other
	 * for re-use..
	 * 
	 * @param mgr
	 * @param parser
	 * @return
	 */
	public abstract HttpClient createHttpClient(ChannelManager mgr, HttpParser parser);
	
	public abstract HttpClient createHttpsClient(ChannelManager mgr, HttpParser parser, HttpsSslEngineFactory factory);
	
}
