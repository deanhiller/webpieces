package org.webpieces.webserver.api;

import org.webpieces.router.api.ProdRouterModule;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.webserver.impl.PortConfigLookupImpl;
import org.webpieces.webserver.impl.WebServerImpl;
import org.webpieces.webserver.impl.WebServerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public abstract class WebServerFactory {

    protected WebServerFactory() {}

	public static WebServer create(WebServerConfig config, RouterConfig routerConfig, TemplateConfig templateConfig, Arguments args) {
		if(!routerConfig.getMetaFile().exists())
			throw new RuntimeException("file not found="+routerConfig.getMetaFile());
		
		Module allModules = getModules(config, routerConfig, templateConfig, args);

		Module platformOverrides = config.getPlatformOverrides();
		if(platformOverrides != null)
			allModules = Modules.override(allModules).with(platformOverrides);

		Injector injector = Guice.createInjector(allModules);
		WebServerImpl serverImpl = injector.getInstance(WebServerImpl.class);
		serverImpl.configureSync(args); //configure must be called as after configured, Arguments.checkConsumedCorrectly must
		  						//be called before start is called on the webserver
		return serverImpl;
	}

	private static Module getModules(WebServerConfig config, RouterConfig routerConfig, TemplateConfig templateConfig, Arguments args) {

		//Special wiring needed between webserver and router due to order of start.  See
		//PortConfigLookupImpl javadoc for more info
		PortConfigLookupImpl portLookup = new PortConfigLookupImpl();
		
		
		return Modules.combine(
			new WebServerModule(config, portLookup, args),
			new ProdRouterModule(routerConfig, portLookup),
			new ProdTemplateModule(templateConfig)
		);
	}
}
