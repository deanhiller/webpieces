package org.webpieces.webserver.impl;

import org.webpieces.webserver.api.WebServer;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.api.WebServerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class WebServerFactoryImpl extends WebServerFactory {

	@Override
	protected WebServer createImpl(String id, Module overrideModule, WebServerConfig config) {
		Module testModule = overrideModule;
		
		Module allModules = getModules(config);
		if(testModule != null) 
			allModules = Modules.override(allModules).with(testModule);
		Injector injector = Guice.createInjector(allModules);
		return injector.getInstance(WebServer.class);
	}

	private Module getModules(WebServerConfig config) {
		return Modules.combine(
			new WebServerModule(config)
		);
	}

}
