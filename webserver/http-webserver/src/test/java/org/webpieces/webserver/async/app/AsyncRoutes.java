package org.webpieces.webserver.async.app;

import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class AsyncRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder router = domainRouteBldr.getAllDomainsRouteBuilder();
		
		router.addRoute(BOTH, HttpMethod.GET, "/",                 "../../basic/app/biz/AsyncController.someMethod", AsyncRouteId.SOME_ROUTE);
		router.addRoute(BOTH, HttpMethod.GET, "/redirect/{id}",    "../../basic/app/biz/AsyncController.redirect", AsyncRouteId.REDIRECT_PAGE);
		router.addRoute(BOTH, HttpMethod.GET, "/redirectint/{id}", "../../basic/app/biz/AsyncController.redirectWithInt", AsyncRouteId.REDIRECT2);
		router.addRoute(BOTH, HttpMethod.GET, "/myroute",          "../../basic/app/biz/AsyncController.myMethod", AsyncRouteId.RENDER_PAGE);
		router.addRoute(BOTH, HttpMethod.GET, "/throwNotFound",    "../../basic/app/biz/AsyncController.throwNotFound", AsyncRouteId.THROW_NOT_FOUND);
		router.addRoute(BOTH, HttpMethod.GET, "/asyncSuccessRoute","../../basic/app/biz/AsyncController.asyncMyMethod", AsyncRouteId.ASYNC_SUCCESS);
		router.addRoute(BOTH, HttpMethod.GET, "/asyncFailRoute",   "../../basic/app/biz/AsyncController.asyncFail", AsyncRouteId.ASYNC_FAIL);

		router.setPageNotFoundRoute("../../basic/app/biz/AsyncController.notFound");
		router.setInternalErrorRoute("../../basic/app/biz/AsyncController.internalError");
	}

}
