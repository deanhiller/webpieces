package org.webpieces.router.api;

import org.webpieces.router.impl.ProdRouterService;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.hooks.MetaLoaderProxy;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.loader.ProdLoader;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ProdRouterModule implements Module {

	private HttpRouterConfig config;

	public ProdRouterModule(HttpRouterConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RoutingService.class).to(ProdRouterService.class).asEagerSingleton();
		binder.bind(MetaLoaderProxy.class).to(ProdLoader.class).asEagerSingleton();
		binder.bind(ClassForName.class).to(ProdClassForName.class).asEagerSingleton();
		
		binder.bind(HttpRouterConfig.class).toInstance(config);
	}
	
}
