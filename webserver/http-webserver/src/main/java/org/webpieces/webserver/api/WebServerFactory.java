package org.webpieces.webserver.api;

import org.webpieces.router.api.ProdRouterModule;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.webserver.impl.WebServerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public abstract class WebServerFactory {

    protected WebServerFactory() {}

	public static WebServer create(WebServerConfig config, RouterConfig routerConfig, TemplateConfig templateConfig) {
		if(!routerConfig.getMetaFile().exists())
			throw new RuntimeException("file not found="+routerConfig.getMetaFile());
		
		Module allModules = getModules(config, routerConfig, templateConfig);

		Module platformOverrides = config.getPlatformOverrides();
		if(platformOverrides != null)
			allModules = Modules.override(allModules).with(platformOverrides);

		Injector injector = Guice.createInjector(allModules);
		return injector.getInstance(WebServer.class);
	}

	private static Module getModules(WebServerConfig config, RouterConfig routerConfig, TemplateConfig templateConfig) {
		return Modules.combine(
			new WebServerModule(config),
			new ProdRouterModule(routerConfig),
			new ProdTemplateModule(templateConfig)
		);
	}
}
