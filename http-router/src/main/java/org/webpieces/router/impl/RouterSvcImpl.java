package org.webpieces.router.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.impl.loader.ProdLoader;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Module;

public class RouterSvcImpl implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(RouterSvcImpl.class);
	
	private final RouterConfig config;

	private boolean started = false;
	
	public RouterSvcImpl(VirtualFile modules, Module overrideModule) {
		ProdLoader loader = new ProdLoader();
		config = new ProdRouterConfig(modules, overrideModule, loader);
	}

	//add Route HOOK callback so translate RouteId -> route and route->controller.method to call
	@Override
	public void start() {
		config.load();
		started = true;
	}

	@Override
	public void stop() {
	}

	@Override
	public void processHttpRequests(Request req) {
		if(!started)
			throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
		config.processHttpRequests(req);
		
	}

}
