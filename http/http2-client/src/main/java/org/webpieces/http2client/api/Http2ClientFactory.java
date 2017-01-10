package org.webpieces.http2client.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.http2client.impl.Http2ClientImpl;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;

public abstract class Http2ClientFactory {

	public static Http2Client createHttpClient(int numThreads) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		BufferCreationPool pool = new BufferCreationPool();
		HpackParser hpackParser = HpackParserFactory.createParser(pool, false);
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		Http2ClientEngineFactory parseFactory = new Http2ClientEngineFactory();
		return createHttpClient(mgr, hpackParser, parseFactory);
	}
	
	public static Http2Client createHttpClient(ChannelManager mgr, HpackParser http2Parser, Http2ClientEngineFactory factory) {
		return new Http2ClientImpl(mgr, http2Parser, factory);
	}
}
