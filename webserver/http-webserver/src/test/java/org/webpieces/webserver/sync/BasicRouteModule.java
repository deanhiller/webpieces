package org.webpieces.webserver.sync;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class BasicRouteModule implements RouteModule {

	@Override
	public void configure(Router router, String currentPackage) {
		
		router.addRoute(HttpMethod.GET, "/",                 "basic/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		router.addRoute(HttpMethod.GET, "/redirect/{id}",    "basic/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		router.addRoute(HttpMethod.GET, "/redirectint/{id}", "basic/BasicController.redirectWithInt", BasicRouteId.REDIRECT2);
		router.addRoute(HttpMethod.GET, "/myroute",          "basic/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		router.addRoute(HttpMethod.GET, "/throwNotFound",    "basic/BasicController.throwNotFound", BasicRouteId.THROW_NOT_FOUND);
		
		router.setPageNotFoundRoute("basic/BasicController.notFound");
		router.setInternalErrorRoute("basic/BasicController.internalError");
	}

}
