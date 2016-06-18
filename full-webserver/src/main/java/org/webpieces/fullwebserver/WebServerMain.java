package org.webpieces.fullwebserver;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.routing.RouteModule;

public class WebServerMain {
	private static final Logger log = LoggerFactory.getLogger(WebServerMain.class);
	
	public static void main(String[] args) {
		
		try {
			List<RouteModule> routeModules = new ArrayList<>();
			routeModules.add(new ExampleRouteModule());
			
			//RoutingService server = RouterSvcFactory.create(null, config);
			
			//server.start();
		} catch(Exception e) {
			log.warn("excpeiton", e);
		}
	}
}
