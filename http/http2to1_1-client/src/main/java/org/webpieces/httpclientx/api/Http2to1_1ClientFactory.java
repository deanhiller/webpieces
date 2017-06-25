package org.webpieces.httpclientx.api;

import org.webpieces.data.api.BufferPool;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpClientFactory;
import org.webpieces.httpclientx.impl.Http2ClientProxy;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;

public abstract class Http2to1_1ClientFactory {

	public static Http2Client createHttpClient(String id, int numThreads, BackpressureConfig backPressureConfig) {
		HttpClient client1_1 = HttpClientFactory.createHttpClient(id, numThreads, backPressureConfig);
		return new Http2ClientProxy(client1_1);
	}

	public static Http2Client createHttpClient(String id, ChannelManager mgr, BufferPool pool) {
		HttpParser parser = HttpParserFactory.createParser(pool);
		HttpClient client1_1 = HttpClientFactory.createHttpClient(id, mgr, parser);
		return new Http2ClientProxy(client1_1);
	}
}
