package org.webpieces.webserver.api;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.ProdRouterModule;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.templating.api.ProdTemplateModule;
import org.webpieces.templating.api.TemplateConfig;
import org.webpieces.util.cmdline2.Arguments;
import org.digitalforge.sneakythrow.SneakyThrow;
import org.webpieces.util.filters.Filter;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.webserver.impl.PortConfigLookupImpl;
import org.webpieces.webserver.impl.PortConfiguration;
import org.webpieces.webserver.impl.WebServerImpl;
import org.webpieces.webserver.impl.WebServerModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public abstract class WebServerFactory {

	private static final Logger log = LoggerFactory.getLogger(WebServerFactory.class);

    protected WebServerFactory() {}

	public static WebServer create(WebServerConfig config, RouterConfig routerConfig, TemplateConfig templateConfig, String ... args) {
		if(!routerConfig.getMetaFile().exists())
			throw new RuntimeException("file not found="+routerConfig.getMetaFile());

		try {
			//logback called getLocalhost(resulting in 5 seconds)
			//H2 called getLocalHost like 3 times(resulting in 15 more seconds)
			//overall, it really screwed the startup time!!
			//https://stackoverflow.com/questions/33289695/inetaddress-getlocalhost-slow-to-run-30-seconds/33289897#33289897
			log.info("Checking timing on getLocalHost (seems very bad on many MAC computers) which makes webpieces startup look slow(and we like a fast startup");
			long start = System.currentTimeMillis();
			InetAddress.getLocalHost();
			long totalTimeSeconds = (System.currentTimeMillis() - start) / 1000;
			if(totalTimeSeconds > 3)
				throw new IllegalStateException("Your computer configuration is messed up.  getLocalHost "
						+ "is taking longer\nthan 3 seconds.  FIX THIS NOW!!!  You can typically edit your hosts file\n"
						+ "to do so.  See https://stackoverflow.com/questions/33289695/inetaddress-getlocalhost-slow-to-run-30-seconds/33289897#33289897 for more info");
			
		} catch (UnknownHostException e) {
			throw SneakyThrow.sneak(e);
		}
		
		Module allModules = getModules(config, routerConfig, templateConfig);

		Module platformOverrides = config.getPlatformOverrides();
		if(platformOverrides != null)
			allModules = Modules.override(allModules).with(platformOverrides);

		Injector injector = Guice.createInjector(allModules);
		WebServerImpl serverImpl = injector.getInstance(WebServerImpl.class);
		
		//special case and I HATE statics but if customer swapped in their own FutureUtil, then we replace it with theirs here.  If they didn't
		//this literally just sets the same FutureUtil into the filters
		FutureHelper util = injector.getInstance(FutureHelper.class);
		Filter.setFutureUtil(util);

		//ANYTHING in webpieces that needs command line arguments, we get through injector here and pass
		//to the method that wires args to fields with validation
		PortConfiguration instance = injector.getInstance(PortConfiguration.class);

		serverImpl.configureSync(instance, args); //configure must be called as after configured, Arguments.checkConsumedCorrectly must
		  						//be called before start is called on the webserver
		return serverImpl;
	}

	private static Module getModules(WebServerConfig config, RouterConfig routerConfig, TemplateConfig templateConfig) {

		//Special wiring needed between webserver and router due to order of start.  See
		//PortConfigLookupImpl javadoc for more info
		PortConfigLookupImpl portLookup = new PortConfigLookupImpl();
		
		boolean hasCoreModule = config.getCoreModule() != null;
		
		Module m = Modules.combine(
			new WebServerModule(config, portLookup, hasCoreModule),
			new ProdRouterModule(routerConfig, portLookup),
			new ProdTemplateModule(templateConfig)
		);
		
		if(config.getCoreModule() != null)
			m = Modules.combine(m, config.getCoreModule());
		
		return m;
	}
}
