package org.webpieces.httpclient11.api;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.httpclient11.impl.HttpClientImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.metrics.MetricStrategy;
import org.webpieces.util.threading.NamedThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class HttpClientFactory {

	public static HttpClient createHttpClient(String id, int numThreads, BackpressureConfig backPressureConfig) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		MetricStrategy.monitorExecutor(executor, id);

		BufferCreationPool pool = new BufferCreationPool();
		HttpParser parser = HttpParserFactory.createParser(pool);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, backPressureConfig, executor);
		
		return createHttpClient(id, mgr, parser);		
	}

	public static HttpClient createHttpClient(String id, ChannelManager mgr, HttpParser parser) {
		return new HttpClientImpl(id, mgr, parser);
	}
}
