package org.webpieces.httpproxy.impl;

import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.api.HttpProxyFactory;
import org.webpieces.httpproxy.api.ProxyConfig;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class HttpProxyFactoryImpl extends HttpProxyFactory {

	@Override
	protected HttpProxy createHttpProxyImpl(String id, Module overrideModule, ProxyConfig config) {
		Module testModule = overrideModule;
		
		Module allModules = getModules(config);
		if(testModule != null) 
			allModules = Modules.override(allModules).with(testModule);
		Injector injector = Guice.createInjector(allModules);
		return injector.getInstance(HttpProxy.class);
	}

	private Module getModules(ProxyConfig config) {
		return Modules.combine(
			new HttpProxyModule(config)
		);
	}

}
