package org.webpieces.webserver.api.basic;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class BasicRouteModule implements RouteModule {

	@Override
	public void configure(Router router, String currentPackage) {
		
		router.addRoute(HttpMethod.GET, "/",              "biz/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		router.addRoute(HttpMethod.GET, "/redirect/{id}", "biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		router.addRoute(HttpMethod.GET, "/myroute",       "biz/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		
		router.setPageNotFoundRoute("biz/BasicController.notFound");
		router.setInternalErrorRoute("biz/BasicController.internalError");
	}

}
