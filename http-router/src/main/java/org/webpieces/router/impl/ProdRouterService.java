package org.webpieces.router.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.impl.loader.ProdLoader;

@Singleton
public class ProdRouterService implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(ProdRouterService.class);
	
	private RouteLoader config;
	private ProdLoader loader;

	private boolean started = false;
	
	@Inject
	public ProdRouterService(RouteLoader config, ProdLoader loader) {
		this.config = config;
		this.loader = loader;
	}

	//add Route HOOK callback so translate RouteId -> route and route->controller.method to call
	@Override
	public void start() {
		log.info("Starting PROD server with NO compiling classloader");
		config.load(loader);
		started = true;
	}

	@Override
	public void stop() {
	}

	@Override
	public void processHttpRequests(Request req, ResponseStreamer responseCb) {
		if(!started)
			throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
		MatchResult meta = config.fetchRoute(req);
		
		config.invokeRoute(meta, req, responseCb);
		
	}

}
