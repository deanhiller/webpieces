package org.webpieces.httpproxy.api;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.httpproxy.impl.FrontEndServerManagerImpl;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;

public abstract class HttpFrontendFactory {
	
	protected abstract HttpFrontendManager createHttpProxyImpl();
	
	/**
	 * 
	 * @param id Use for logging and also file recording names
	 * @param threadPoolSize The size of the threadpool, although all data comes in order as we
	 * use the SessionExecutorImpl found in webpieces
	 * 
	 * @return
	 */
	public static HttpFrontendManager createFrontEnd(String id, int threadPoolSize) {
		Executor executor = Executors.newFixedThreadPool(threadPoolSize, new NamedThreadFactory(id));
		BufferCreationPool pool = new BufferCreationPool();
		HttpParserFactory.createParser(pool);
		
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager chanMgr = factory.createMultiThreadedChanMgr(id, pool, executor);
		
		HttpParser parser = HttpParserFactory.createParser(pool);
		
		AsyncServerManager svrMgr = AsyncServerMgrFactory.createAsyncServer(chanMgr);
		
		return createFrontEnd(svrMgr, parser);
	}
	
	public static HttpFrontendManager createFrontEnd(AsyncServerManager svrManager, HttpParser parser) {
		return new FrontEndServerManagerImpl(svrManager, parser);
	}
	
}
