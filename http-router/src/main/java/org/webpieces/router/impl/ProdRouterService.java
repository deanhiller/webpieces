package org.webpieces.router.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.impl.loader.ClassForName;
import org.webpieces.router.impl.loader.ProdClassForName;

@Singleton
public class ProdRouterService extends AbstractRouterService implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(ProdRouterService.class);
	
	private RouteLoader routeLoader;
	private ClassForName loader;
	
	@Inject
	public ProdRouterService(RouteLoader routeLoader, ProdClassForName loader) {
		this.routeLoader = routeLoader;
		this.loader = loader;
	}

	//add Route HOOK callback so translate RouteId -> route and route->controller.method to call
	@Override
	public void start() {
		log.info("Starting PROD server with NO compiling classloader");
		
		routeLoader.load(loader);
		started = true;
	}

	@Override
	public void stop() {
		started = false;
	}

	@Override
	public void processHttpRequestsImpl(RouterRequest req, ResponseStreamer responseCb) {
		MatchResult meta = routeLoader.fetchRoute(req);
		
		routeLoader.invokeRoute(meta, req, responseCb, () -> routeLoader.fetchNotFoundRoute());
	}

}
