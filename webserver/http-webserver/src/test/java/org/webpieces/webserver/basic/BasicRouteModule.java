package org.webpieces.webserver.basic;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class BasicRouteModule implements RouteModule {

	@Override
	public void configure(Router router, String currentPackage) {
		
		router.addRoute(HttpMethod.GET, "/",                 "biz/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		router.addRoute(HttpMethod.GET, "/redirect/{id}",    "biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		router.addRoute(HttpMethod.GET, "/redirectint/{id}", "biz/BasicController.redirectWithInt", BasicRouteId.REDIRECT2);
		router.addRoute(HttpMethod.GET, "/myroute",          "biz/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		router.addRoute(HttpMethod.GET, "/throwNotFound",    "biz/BasicController.throwNotFound", BasicRouteId.THROW_NOT_FOUND);
		router.addRoute(HttpMethod.GET, "/pageparam",        "biz/BasicController.pageParam", BasicRouteId.PAGE_PARAM);
		router.addRoute(HttpMethod.GET, "/verbatim",         "biz/BasicController.verbatimTag", BasicRouteId.VERBATIM);

		router.addRoute(HttpMethod.GET, "/if",               "biz/BasicController.ifTag", BasicRouteId.IF);
		router.addRoute(HttpMethod.GET, "/else",             "biz/BasicController.elseTag", BasicRouteId.ELSE);
		router.addRoute(HttpMethod.GET, "/elseif",           "biz/BasicController.elseIfTag", BasicRouteId.ELSEIF);

		router.setPageNotFoundRoute("biz/BasicController.notFound");
		router.setInternalErrorRoute("biz/BasicController.internalError");
	}

}
