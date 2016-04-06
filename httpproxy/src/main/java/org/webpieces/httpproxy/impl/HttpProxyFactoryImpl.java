package org.webpieces.httpproxy.impl;

import java.util.Map;

import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.api.HttpProxyFactory;
import org.webpieces.httpproxy.api.HttpProxyService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class HttpProxyFactoryImpl extends HttpProxyFactory {

	@Override
	protected HttpProxy createHttpProxyImpl(String id, Map<String, Object> props) {
		Module testModule = (Module) props.get(OVERRIDE_MODULE);
		
		Module allModules = getModules();
		if(testModule != null) 
			allModules = Modules.override(allModules).with(testModule);
		Injector injector = Guice.createInjector(allModules);
		return injector.getInstance(HttpProxy.class);
	}

	private Module getModules() {
		return Modules.combine(
			new HttpProxyModule()
		);
	}

	@Override
	protected HttpProxyService createHttpProxyService() {
		return null;
	}

}
