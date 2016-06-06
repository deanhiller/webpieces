package org.webpieces.router.api;

import org.webpieces.router.impl.ProdRouterConfig;
import org.webpieces.router.impl.RouterConfig;
import org.webpieces.router.impl.RouterSvcImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

public class HttpRouterModule implements Module {

	private HttpRouterConfig config;

	public HttpRouterModule(HttpRouterConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RoutingService.class).to(RouterSvcImpl.class).asEagerSingleton();;
		binder.bind(RouterConfig.class).to(ProdRouterConfig.class);
		binder.bind(HttpRouterConfig.class).toInstance(config);
	}
	
}
