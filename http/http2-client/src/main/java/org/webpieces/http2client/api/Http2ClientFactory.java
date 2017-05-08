package org.webpieces.http2client.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.impl.Http2ClientImpl;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.util.threading.SessionExecutor;
import org.webpieces.util.threading.SessionExecutorImpl;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;
import com.webpieces.http2engine.api.client.Http2Config;

public abstract class Http2ClientFactory {

	public static Http2Client createHttpClient(int numThreads) {
		Http2Config config = new Http2Config();
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		BufferCreationPool pool = new BufferCreationPool();
		HpackParser hpackParser = HpackParserFactory.createParser(pool, false);
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);

		Http2ClientEngineFactory parseFactory = new Http2ClientEngineFactory();
		return createHttpClient(config, mgr, hpackParser, parseFactory, executor);
	}
	
	public static Http2Client createHttpClient(Http2Config config, ChannelManager mgr, Executor executor) {
		Http2ClientEngineFactory engineFactory = new Http2ClientEngineFactory();
		BufferCreationPool pool = new BufferCreationPool();
		HpackParser hpackParser = HpackParserFactory.createParser(pool, false);
		
		return createHttpClient(config, mgr, hpackParser, engineFactory, executor);
	}
	
	public static Http2Client createHttpClient(
			Http2Config config, ChannelManager mgr, HpackParser hpackParser, Http2ClientEngineFactory engineFactory, Executor executor) {
		SessionExecutor sessionExecutor = new SessionExecutorImpl(executor);
		return new Http2ClientImpl(config, mgr, hpackParser, engineFactory, sessionExecutor);
	}
}
