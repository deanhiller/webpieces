package org.webpieces.router.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.hooks.ClassForName;
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
		
		ProdErrorRoutes errorRoutes = new ProdErrorRoutes(routeLoader);
		routeLoader.invokeRoute(meta, req, responseCb, errorRoutes);
	}

	//This only exists so dev mode can swap it out and load error routes dynamically as code changes..
	private static class ProdErrorRoutes implements ErrorRoutes {
		private RouteLoader routeLoader;
		public ProdErrorRoutes(RouteLoader routeLoader) {
			this.routeLoader = routeLoader;
		}

		public MatchResult fetchNotfoundRoute(NotFoundException e) {
			//not found is normal in prod mode so we don't log that and only log warnings in dev mode
			return routeLoader.fetchNotFoundRoute();
		}
		
		public MatchResult fetchInternalServerErrorRoute() {
			return routeLoader.fetchInternalErrorRoute();
		}
	}
}
