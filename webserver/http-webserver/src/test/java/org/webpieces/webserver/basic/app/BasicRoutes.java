package org.webpieces.webserver.basic.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class BasicRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder router = domainRouteBldr.getAllDomainsRouteBuilder();
		router.addRoute(BOTH, GET , "/",                  "biz/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		router.addRoute(BOTH, GET , "/redirect/{id}",     "biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		router.addRoute(BOTH, GET , "/redirectint/{id}",  "biz/BasicController.redirectWithInt", BasicRouteId.REDIRECT2);
		router.addRoute(BOTH, GET , "/myroute",           "biz/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		router.addRoute(BOTH, GET , "/myroute2",           "biz/BasicController.myMethodFullPath", BasicRouteId.RENDER_PAGE2);

		router.addRoute(BOTH, GET , "/throwNotFound",     "biz/BasicController.throwNotFound", BasicRouteId.THROW_NOT_FOUND);
		router.addRoute(BOTH, GET , "/badtemplate",       "biz/BasicController.badTemplate", BasicRouteId.BAD_TEMPLATE);
		router.addRoute(BOTH, GET , "/somejson",          "biz/BasicController.jsonFile", BasicRouteId.JSON_ROUTE);
		router.addRoute(BOTH, GET , "/rawurlredirect",    "/org/webpieces/webserver/basic/app/biz/BasicController.redirectRawUrl", BasicRouteId.REDIRECT_RAW_URL);
		router.addRoute(BOTH, GET , "/rawabsoluteurlredirect",    "biz/BasicController.redirectRawAbsoluteUrl", BasicRouteId.REDIRECT_ABSOLUTE_URL);

		router.addRoute(BOTH, GET , "/null",              "biz/BasicController.returnNull", BasicRouteId.NULL_ROUTE);

		// #{form action:@POST_CATCH_ALL[:] id:'detailsForm'}#  ....   #{/form}#
		//addPostRoute("/post/{_controller}/{_action}", "{_controller}.post{_action}", BasicRouteId.POST_CATCH_ALL); //catch all post route
		
		router.setPageNotFoundRoute("biz/BasicController.notFound");
		router.setInternalErrorRoute("biz/BasicController.internalError");
	}

}
