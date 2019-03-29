package org.webpieces.devrouter.api;

import javax.inject.Singleton;

import org.webpieces.compiler.api.CompileConfig;
import org.webpieces.compiler.api.CompileOnDemand;
import org.webpieces.compiler.api.CompileOnDemandFactory;
import org.webpieces.devrouter.impl.DevClassForName;
import org.webpieces.devrouter.impl.DevCompressionCacheSetup;
import org.webpieces.devrouter.impl.DevLoader;
import org.webpieces.devrouter.impl.DevRouteInvoker;
import org.webpieces.devrouter.impl.DevRoutingService;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

public class DevRouterModule implements Module {
	
	private CompileConfig config;

	public DevRouterModule(CompileConfig config) {
		this.config = config;
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(RouterService.class).to(DevRoutingService.class).asEagerSingleton();;
		binder.bind(MetaLoaderProxy.class).to(DevLoader.class).asEagerSingleton();
		binder.bind(ClassForName.class).to(DevClassForName.class).asEagerSingleton();
		binder.bind(CompressionCacheSetup.class).to(DevCompressionCacheSetup.class).asEagerSingleton();
		binder.bind(RouteInvoker2.class).to(DevRouteInvoker.class).asEagerSingleton();
	}
	
	@Provides
	@Singleton
	public CompileOnDemand provideCompile(RouterConfig routerConfig) {
		if(routerConfig.getWebappOverrides() != null)
			throw new IllegalArgumentException("In DEVELOPMENT server mode, when you provide"
					+ " HttpRouterConfig, you cannot have a app OverridesModule set as "
					+ "this would be a classloader headache foor you.  Check our tests to see for another possible method ");
		CompileOnDemand onDemand = CompileOnDemandFactory.createCompileOnDemand(config);
		return onDemand;
	}

}
