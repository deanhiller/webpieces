package org.webpieces.webserver.api;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class MyTestRouteModule implements RouteModule {

	@Override
	public void configure(Router router, String currentPackage) {

		router.addRoute(HttpMethod.GET, "/", "MyTestController.redirect", MyTestRouteId.REDIRECT_PAGE);

		router.addRoute(HttpMethod.GET, "/render", "MyTestController.render", MyTestRouteId.RENDER_PAGE);
		
		router.setNotFoundRoute("MyTestController.notFound");
	}

}
