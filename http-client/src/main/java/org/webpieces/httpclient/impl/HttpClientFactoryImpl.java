package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.nio.api.BufferCreationPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;

public class HttpClientFactoryImpl extends HttpClientFactory {

	@Override
	public HttpClient createHttpClient(ChannelManager mgr, HttpParser parser, BufferCreationPool pool) {
		return new HttpClientImpl(mgr, parser, pool);
	}

	@Override
	public HttpClient createHttpClient() {
		HttpParser parser = HttpParserFactory.createParser();
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		BufferCreationPool pool = new BufferCreationPool(false, 2000);
		ChannelManager mgr = factory.createChannelManager("httpClientChanMgr", pool);
		
		return createHttpClient(mgr, parser, pool);
	}

}
