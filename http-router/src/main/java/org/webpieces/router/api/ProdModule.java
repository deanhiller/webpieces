package org.webpieces.router.api;

import org.webpieces.router.impl.ProdRouterService;
import org.webpieces.router.impl.loader.ClassForName;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.loader.ProdLoader;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ProdModule implements Module {

	private HttpRouterConfig config;

	public ProdModule(HttpRouterConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RoutingService.class).to(ProdRouterService.class).asEagerSingleton();
		binder.bind(ControllerLoader.class).to(ProdLoader.class).asEagerSingleton();
		binder.bind(ClassForName.class).to(ProdClassForName.class).asEagerSingleton();
		
		binder.bind(HttpRouterConfig.class).toInstance(config);
	}
	
}
