package org.webpieces.webserver.https.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routes.Port.BOTH;
import static org.webpieces.router.api.routes.Port.HTTPS;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.webserver.api.login.LoginFilter;
import org.webpieces.webserver.api.login.LoginInfo;

public class HttpsRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		
		bldr.addRoute(HTTPS, GET , "/secureRoute",         "HttpsController.home", HttpsRouteId.HOME);
		bldr.addRoute(HTTPS, GET , "/login",               "HttpsController.login", HttpsRouteId.LOGIN);
		bldr.addRoute(HTTPS, POST, "/postLogin",           "HttpsController.postLogin", HttpsRouteId.POST_LOGIN, false);
		bldr.addRoute(HTTPS, GET , "/secure/internal",     "HttpsController.internal", HttpsRouteId.INTERNAL);

		//in this case, https route of same path is never hit since the order is wrong...
		bldr.addRoute(BOTH, GET ,     "/same",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE);
		bldr.addRoute(HTTPS, GET, "/same",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE);
		
		//This is the correct order, https://.../same2 shows the https route and http://.../same2 shows the http route...
		bldr.addRoute(HTTPS, GET, "/same2",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE2);		
		bldr.addRoute(BOTH, GET ,     "/same2",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE2);

		bldr.addRoute(BOTH, GET ,     "/backendlogin",      "HttpsController.httpRoute", HttpsRouteId.LOGIN_BACKEND);

		//Unlike routes which apply regex to request urls, filters regexs are applied to route regexs so if a filter
		//matches a route, it will be added to all requests for that route.  This is done so we don't have to
		//figure out which filters to apply on each request and on startup can wire up all filters once
		bldr.addFilter("/secure/.*", LoginFilter.class, new LoginInfo(HttpsController.LOGIN_TOKEN, HttpsRouteId.LOGIN), FilterPortType.HTTPS_FILTER, 0);
		bldr.addFilter("/backend/.*", LoginFilter.class, new LoginInfo("mgrId", HttpsRouteId.LOGIN_BACKEND), FilterPortType.HTTPS_FILTER, 0);
	
		bldr.addNotFoundFilter(LoginFilter.class, new LoginInfo(HttpsController.LOGIN_TOKEN, HttpsRouteId.LOGIN), FilterPortType.HTTPS_FILTER, 0);
		bldr.addNotFoundFilter(LoginFilter.class, new LoginInfo("mgrId", HttpsRouteId.LOGIN_BACKEND), FilterPortType.HTTPS_FILTER, 0);

		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
