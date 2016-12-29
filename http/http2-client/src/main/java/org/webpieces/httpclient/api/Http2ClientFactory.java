package org.webpieces.httpclient.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.httpclient.impl2.Http2ClientImpl;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

public abstract class Http2ClientFactory {

	public static Http2Client createHttpClient(int numThreads) {
		Executor executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("httpclient"));
		BufferCreationPool pool = new BufferCreationPool();
		Http2Parser http2Parser = Http2ParserFactory.createParser(pool);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("httpClientChanMgr", pool, executor);
		
		return createHttpClient(mgr, http2Parser);
	}
	
	public static Http2Client createHttpClient(ChannelManager mgr, Http2Parser http2Parser) {
		return new Http2ClientImpl(mgr, http2Parser);
	}
}
