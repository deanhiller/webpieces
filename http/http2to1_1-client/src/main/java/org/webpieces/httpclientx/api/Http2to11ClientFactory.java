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

import io.micrometer.core.instrument.MeterRegistry;

public abstract class Http2to11ClientFactory {

	public static Http2Client createHttpClient(String id, int numThreads, BackpressureConfig backPressureConfig, MeterRegistry metrics) {
		HttpClient client11 = HttpClientFactory.createHttpClient(id, numThreads, backPressureConfig, metrics);
		return new Http2ClientProxy(client11);
	}

	public static Http2Client createHttpClient(String id, ChannelManager mgr, MeterRegistry metrics, BufferPool pool) {
		HttpParser parser = HttpParserFactory.createParser(id, metrics, pool);
		HttpClient client11 = HttpClientFactory.createHttpClient(id, mgr, parser);
		return new Http2ClientProxy(client11);
	}
}
