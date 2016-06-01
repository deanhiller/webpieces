package org.webpieces.router.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.Route;
import org.webpieces.router.api.RouteModule;
import org.webpieces.router.api.Router;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.dto.RouterConfig;
import org.webpieces.router.api.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class RouterSvcImpl implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(RouterSvcImpl.class);
	
	private final RouterConfig config;
	private RouteInfo routeInfo = new RouteInfo();
	private ReverseRoutes reverseRoutes = new ReverseRoutes();
	private boolean started = false;
	private Map<Route, Object> routeToController = new HashMap<>();

	private Module overrideModule;

	private Injector injector;
	
	public RouterSvcImpl(VirtualFile modules, Module overrideModule) {
		this.overrideModule = overrideModule;
		config = new RouterConfig(modules);
	}

	//add Route HOOK callback so translate RouteId -> route and route->controller.method to call
	@Override
	public void start() {
		log.info("load routes");

		//create and setup all mapping for routes in production mode
		//in dev mode(different class), we do this on request path such that we
		//can quickly recompile any changes to routes we need on each request so server restarts
		//are not necessary
		addRoutes();
		
		if(!routeInfo.isCatchallRouteSet())
			throw new IllegalStateException("Client RouterModule did not call top level router.setCatchAllRoute");
		
		log.info("loading controllers from RouterModules");
		
		injector = createInjector();
		
		//create all 'known' controllers for production mode in start() method
		//in dev mode we create on request path to speed startup up
		for(Route r: reverseRoutes.getAllRoutes()) {
			//fetch controller...
			Object controller = r.getController(injector);
			routeToController.put(r, controller);
			
			verifyController(r, controller);
		}

		started = true;
		log.info("now listening for incoming connections");
	}

	private void verifyController(Route r, Object controller) {
		
	}

	private Injector createInjector() {
		List<Module> guiceModules = config.getGuiceModules();
		
		Module module = Modules.combine(guiceModules);
		if(overrideModule != null)
			module = Modules.override(module).with(overrideModule);
		
		Injector injector = Guice.createInjector(module);
		return injector;
	}

	public void addRoutes() {
		log.info("adding routes");
		Router router = new RouterImpl(routeInfo, reverseRoutes);
		
		for(RouteModule module : config.getRouteModules()) {
			module.configure(router);
		}
		
		log.info("added all routes to router");
	}

	@Override
	public void stop() {
	}

	@Override
	public void processHttpRequests(Request req) {
		if(!started)
			throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
		Route route = routeInfo.fetchRoute(req, req.relativePath);
		if(route == null)
			throw new IllegalStateException("missing exception on creation if we go this far");
		
		

		
	}

}
