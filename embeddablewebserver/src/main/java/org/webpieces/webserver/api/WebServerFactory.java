package org.webpieces.webserver.api;

import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.ProdRouterModule;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.webserver.impl.WebServerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public abstract class WebServerFactory {
	
    protected WebServerFactory() {}

    public static WebServer create(HttpRouterConfig config) {
    	return create(new WebServerConfig(), config);
    }
    
	public static WebServer create(WebServerConfig config, HttpRouterConfig routerConfig) {
		Module allModules = getModules(config, routerConfig);
		
		Module platformOverrides = config.getPlatformOverrides();
		if(platformOverrides != null) 
			allModules = Modules.override(allModules).with(platformOverrides);

		Injector injector = Guice.createInjector(allModules);
		return injector.getInstance(WebServer.class);
	}

	private static Module getModules(WebServerConfig config, HttpRouterConfig routerConfig) {
		return Modules.combine(
			new WebServerModule(config),
			new ProdRouterModule(routerConfig),
			new ProdTemplateModule()
		);
	}
}
