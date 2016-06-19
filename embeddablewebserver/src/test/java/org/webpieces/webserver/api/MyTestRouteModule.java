package org.webpieces.webserver.api;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class MyTestRouteModule implements RouteModule {

	@Override
	public void configure(Router router, String currentPackage) {

		router.setNotFoundRoute("MyTestController.notFound");
	}

}
