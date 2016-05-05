package org.webpieces.httpproxy.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;
import org.webpieces.util.threading.SessionExecutor;
import org.webpieces.util.threading.SessionExecutorImpl;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.BufferPool;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;

public class HttpProxyModule implements Module {

//	private static final String PROMISE_EXECUTOR = "promiseExecutor";

	@Override
	public void configure(Binder binder) {
		binder.bind(HttpProxy.class).to(HttpProxyImpl.class);

		//needs to be shared...
		BufferCreationPool pool = new BufferCreationPool();
		binder.bind(BufferPool.class).toInstance(pool);
		binder.bind(HttpParser.class).toInstance(HttpParserFactory.createParser(pool));
		binder.bind(DataWrapperGenerator.class).toInstance(DataWrapperGeneratorFactory.createDataWrapperGenerator());
	}

	@Named("chanMgr")
	@Provides
	@Singleton
	public ChannelManager providesChannelManager(BufferPool pool) {
		Executor executor = Executors.newFixedThreadPool(25, new NamedThreadFactory("httpproxy-"));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		return factory.createChannelManager("httpProxyMgr", pool, executor);
	}
	
	@Provides
	@Singleton
	public AsyncServerManager providesAsyncServerMgr(@Named("chanMgr") ChannelManager mgr) {
		return AsyncServerMgrFactory.createAsyncServer(mgr);
	}
	
	@Provides
	@Singleton
	public HttpClient provideHttpClient() {
		BufferCreationPool pool = new BufferCreationPool();
		Executor executor = Executors.newFixedThreadPool(25, new NamedThreadFactory("httpclient-"));
		ChannelManagerFactory cmFactory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = cmFactory.createChannelManager("httpclient", pool, executor);		
		HttpParser parser = HttpParserFactory.createParser(pool);
		
		HttpClientFactory factory = HttpClientFactory.createFactory();
		return factory.createHttpClient(mgr, parser);
	}
}
