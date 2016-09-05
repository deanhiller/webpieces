package org.webpieces.router.api;

import org.webpieces.router.impl.ProdRouterService;
import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.compression.ProdCompressionCacheSetup;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.loader.ProdLoader;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ProdRouterModule implements Module {

	private RouterConfig config;

	public ProdRouterModule(RouterConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RoutingService.class).to(ProdRouterService.class).asEagerSingleton();
		binder.bind(MetaLoaderProxy.class).to(ProdLoader.class).asEagerSingleton();
		binder.bind(ClassForName.class).to(ProdClassForName.class).asEagerSingleton();
		binder.bind(CompressionCacheSetup.class).to(ProdCompressionCacheSetup.class).asEagerSingleton();;
		
		binder.bind(RouterConfig.class).toInstance(config);
	}
	
}
