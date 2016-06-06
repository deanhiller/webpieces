package org.webpieces.router.impl;

import javax.inject.Inject;

import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.impl.loader.ProdLoader;

public class ProdRouterConfig extends RouterConfig {

	@Inject
	public ProdRouterConfig(HttpRouterConfig config, ProdLoader loader) {
		super(config, loader);
	}

	@Override
	public void processHttpRequests(Request req) {
		RouteMeta meta = fetchRoute(req);
		
		invokeRoute(meta, req);
	}

}
