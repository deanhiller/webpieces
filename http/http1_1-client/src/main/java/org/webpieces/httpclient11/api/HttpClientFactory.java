package org.webpieces.httpclient11.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.TwoPools;
import org.webpieces.httpclient11.impl.HttpClientImpl;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

public abstract class HttpClientFactory {

	public static HttpClient createHttpClient(String id, int numThreads, BackpressureConfig backPressureConfig, MeterRegistry metrics) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		ExecutorServiceMetrics.monitor(metrics, executor, id);

		TwoPools pool = new TwoPools(id+".bufferpool", metrics);
		HttpParser parser = HttpParserFactory.createParser(id, metrics, pool);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(metrics);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, backPressureConfig, executor);
		
		return createHttpClient(id, mgr, parser);		
	}

	public static HttpClient createHttpClient(String id, ChannelManager mgr, HttpParser parser) {
		return new HttpClientImpl(id, mgr, parser);
	}
}
