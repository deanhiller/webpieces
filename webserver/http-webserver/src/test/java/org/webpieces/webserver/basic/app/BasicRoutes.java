package org.webpieces.webserver.basic.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class BasicRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/",                  "biz/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		bldr.addRoute(BOTH, GET , "/redirect/{id}",     "biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		bldr.addRoute(BOTH, GET , "/redirectint/{id}",  "biz/BasicController.redirectWithInt", BasicRouteId.REDIRECT2);
		bldr.addRoute(BOTH, GET , "/myroute",           "biz/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		bldr.addRoute(BOTH, GET , "/myroute2",           "biz/BasicController.myMethodFullPath", BasicRouteId.RENDER_PAGE2);

		bldr.addRoute(BOTH, GET , "/throwNotFound",     "biz/BasicController.throwNotFound", BasicRouteId.THROW_NOT_FOUND);
		bldr.addRoute(BOTH, GET , "/badtemplate",       "biz/BasicController.badTemplate", BasicRouteId.BAD_TEMPLATE);
		bldr.addRoute(BOTH, GET , "/somejson",          "biz/BasicController.jsonFile", BasicRouteId.JSON_ROUTE);
		bldr.addRoute(BOTH, GET , "/rawurlredirect",    "/org/webpieces/webserver/basic/app/biz/BasicController.redirectRawUrl", BasicRouteId.REDIRECT_RAW_URL);
		bldr.addRoute(BOTH, GET , "/rawabsoluteurlredirect",    "biz/BasicController.redirectRawAbsoluteUrl", BasicRouteId.REDIRECT_ABSOLUTE_URL);

		bldr.addRoute(BOTH, GET , "/null",              "biz/BasicController.returnNull", BasicRouteId.NULL_ROUTE);

		// #{form action:@POST_CATCH_ALL[:] id:'detailsForm'}#  ....   #{/form}#
		//addPostRoute("/post/{_controller}/{_action}", "{_controller}.post{_action}", BasicRouteId.POST_CATCH_ALL); //catch all post route
		
		bldr.setPageNotFoundRoute("biz/BasicController.notFound");
		bldr.setInternalErrorRoute("biz/BasicController.internalError");
	}

}
