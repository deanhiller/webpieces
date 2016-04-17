package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.nio.api.ChannelManager;

import com.webpieces.httpparser.api.HttpParser;

public class HttpClientFactoryImpl extends HttpClientFactory {

	@Override
	public HttpClient createHttpClient(ChannelManager mgr, HttpParser parser) {
		return new HttpClientImpl(mgr, parser);
	}

}
