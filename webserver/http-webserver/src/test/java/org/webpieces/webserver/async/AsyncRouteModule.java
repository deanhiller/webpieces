package org.webpieces.webserver.async;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class AsyncRouteModule implements RouteModule {

	@Override
	public void configure(Router router, String currentPackage) {
		
		router.addRoute(HttpMethod.GET, "/",                 "../basic/biz/AsyncController.someMethod", AsyncRouteId.SOME_ROUTE);
		router.addRoute(HttpMethod.GET, "/redirect/{id}",    "../basic/biz/AsyncController.redirect", AsyncRouteId.REDIRECT_PAGE);
		router.addRoute(HttpMethod.GET, "/redirectint/{id}", "../basic/biz/AsyncController.redirectWithInt", AsyncRouteId.REDIRECT2);
		router.addRoute(HttpMethod.GET, "/myroute",          "../basic/biz/AsyncController.myMethod", AsyncRouteId.RENDER_PAGE);
		router.addRoute(HttpMethod.GET, "/throwNotFound",    "../basic/biz/AsyncController.throwNotFound", AsyncRouteId.THROW_NOT_FOUND);
		
		router.setPageNotFoundRoute("../basic/biz/AsyncController.notFound");
		router.setInternalErrorRoute("../basic/biz/AsyncController.internalError");
	}

}
