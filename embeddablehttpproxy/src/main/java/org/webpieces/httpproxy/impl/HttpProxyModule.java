package org.webpieces.httpproxy.impl;

import javax.inject.Singleton;

import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.nio.api.BufferCreationPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;

public class HttpProxyModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(HttpProxy.class).to(HttpProxyImpl.class);

		binder.bind(HttpParser.class).toInstance(HttpParserFactory.createParser());
		binder.bind(DataWrapperGenerator.class).toInstance(DataWrapperGeneratorFactory.createDataWrapperGenerator());
	}

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
		return AsyncServerMgrFactory.createChannelManager(mgr);
	}
	
}
