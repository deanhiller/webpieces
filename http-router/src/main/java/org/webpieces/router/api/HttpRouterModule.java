package org.webpieces.router.api;

import org.webpieces.router.impl.ProdRouterService;

import com.google.inject.Binder;
import com.google.inject.Module;

public class HttpRouterModule implements Module {

	private HttpRouterConfig config;

	public HttpRouterModule(HttpRouterConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RoutingService.class).to(ProdRouterService.class).asEagerSingleton();;
		binder.bind(HttpRouterConfig.class).toInstance(config);
	}
	
}
