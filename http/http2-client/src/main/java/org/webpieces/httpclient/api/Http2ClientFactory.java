package org.webpieces.httpclient.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.httpclient.impl2.Http2ClientImpl;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.http2engine.api.Http2HighLevelFactory;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;

public abstract class Http2ClientFactory {

	public static Http2Client createHttpClient(int numThreads) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		BufferCreationPool pool = new BufferCreationPool();
		Http2Parser2 http2Parser = Http2ParserFactory.createParser2(pool);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		Http2HighLevelFactory parseFactory = new Http2HighLevelFactory();
		return createHttpClient(mgr, http2Parser, parseFactory);
	}
	
	public static Http2Client createHttpClient(ChannelManager mgr, Http2Parser2 http2Parser, Http2HighLevelFactory factory) {
		return new Http2ClientImpl(mgr, http2Parser, factory);
	}
}
