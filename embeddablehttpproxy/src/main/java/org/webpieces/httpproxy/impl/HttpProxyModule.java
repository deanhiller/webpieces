package org.webpieces.httpproxy.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.nio.api.BufferCreationPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;

public class HttpProxyModule implements Module {

//	private static final String PROMISE_EXECUTOR = "promiseExecutor";

	@Override
	public void configure(Binder binder) {
		binder.bind(HttpProxy.class).to(HttpProxyImpl.class);

		binder.bind(HttpParser.class).toInstance(HttpParserFactory.createParser());
		binder.bind(DataWrapperGenerator.class).toInstance(DataWrapperGeneratorFactory.createDataWrapperGenerator());
	}

	@Provides
	@Singleton
	public Executor createExecutorPool() {
		ExecutorService pool = Executors.newFixedThreadPool(25, new NamedThreadFactory("httpproxy-"));
		return pool;
	}
	
//	@Provides
//	@Singleton
//	@Named(PROMISE_EXECUTOR)
//	public Executor provideExecutor() {
//		return Executors.newFixedThreadPool(1, new NamedThreadFactory("promiseExecutor"));
//	}
	
	@Provides
	@Singleton
	public BufferCreationPool providesBufferPool() {
		return new BufferCreationPool(false, 2000);
	}
	
	@Provides
	@Singleton
	public ChannelManager providesChannelManager(BufferCreationPool pool) {
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		return factory.createChannelManager("httpProxyMgr", pool);
	}
	
	@Provides
	@Singleton
	public AsyncServerManager providesAsyncServerMgr(ChannelManager mgr) {
		return AsyncServerMgrFactory.createAsyncServer(mgr);
	}
	
}
