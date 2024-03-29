package org.webpieces.http2client.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.TwoPools;
import org.webpieces.data.api.BufferPool;
import org.webpieces.http2client.impl.Http2ClientImpl;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.metrics.MetricsCreator;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.util.time.TimeImpl;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;
import com.webpieces.http2engine.api.client.Http2Config;
import com.webpieces.http2engine.api.client.InjectionConfig;

import io.micrometer.core.instrument.MeterRegistry;

public abstract class Http2ClientFactory {

	public static Http2Client createHttpClient(Http2ClientConfig config, MeterRegistry metrics) {
		Executor executor = Executors.newFixedThreadPool(config.getNumThreads(), new NamedThreadFactory("httpclient"));
		MetricsCreator.monitor(metrics, executor, config.getId());

		TwoPools pool = new TwoPools(config.getId()+".bufferpool", metrics);
		HpackParser hpackParser = HpackParserFactory.createParser(pool, false);
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(metrics);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, config.getBackpressureConfig(), executor);

		InjectionConfig injConfig = new InjectionConfig(hpackParser, new TimeImpl(), config.getHttp2Config());
		return createHttpClient(config.getHttp2Config().getId(), mgr, injConfig);
	}
	
	public static Http2Client createHttpClient(Http2Config config, ChannelManager mgr, BufferPool pool) {
		HpackParser hpackParser = HpackParserFactory.createParser(pool, false);
		
		InjectionConfig injConfig = new InjectionConfig(hpackParser, new TimeImpl(), config);

		return createHttpClient(config.getId(), mgr, injConfig);
	}
	
	public static Http2Client createHttpClient(String id, ChannelManager mgr, InjectionConfig injectionConfig) {
		Http2ClientEngineFactory engineFactory = new Http2ClientEngineFactory(injectionConfig);
		return new Http2ClientImpl(id, mgr, engineFactory );
	}
}
