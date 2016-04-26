package org.webpieces.httpclient.api;

import org.webpieces.httpclient.impl.HttpClientFactoryImpl;
import org.webpieces.nio.api.BufferCreationPool;
import org.webpieces.nio.api.ChannelManager;

import com.webpieces.httpparser.api.HttpParser;

public abstract class HttpClientFactory {

	public static HttpClientFactory createFactory() {
		return new HttpClientFactoryImpl();
	}

	public abstract HttpClient createHttpClient();
	
	public abstract HttpClient createHttpClient(
			ChannelManager mgr, HttpParser parser,
			BufferCreationPool pool);
}
