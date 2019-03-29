package org.webpieces.webserver.https.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routing.Port.BOTH;
import static org.webpieces.router.api.routing.Port.HTTPS;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.webserver.api.login.LoginFilter;
import org.webpieces.webserver.api.login.LoginInfo;

public class HttpsRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder router = domainRouteBldr.getAllDomainsRouteBuilder();
		
		router.addRoute(HTTPS, GET , "/secureRoute",         "HttpsController.home", HttpsRouteId.HOME);
		router.addRoute(HTTPS, GET , "/login",               "HttpsController.login", HttpsRouteId.LOGIN);
		router.addRoute(HTTPS, POST, "/postLogin",           "HttpsController.postLogin", HttpsRouteId.POST_LOGIN, false);
		router.addRoute(HTTPS, GET , "/secure/internal",     "HttpsController.internal", HttpsRouteId.INTERNAL);

		//in this case, https route of same path is never hit since the order is wrong...
		router.addRoute(BOTH, GET ,     "/same",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE);
		router.addRoute(HTTPS, GET, "/same",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE);
		
		//This is the correct order, https://.../same2 shows the https route and http://.../same2 shows the http route...
		router.addRoute(HTTPS, GET, "/same2",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE2);		
		router.addRoute(BOTH, GET ,     "/same2",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE2);

		router.addRoute(BOTH, GET ,     "/backendlogin",      "HttpsController.httpRoute", HttpsRouteId.LOGIN_BACKEND);

		//Unlike routes which apply regex to request urls, filters regexs are applied to route regexs so if a filter
		//matches a route, it will be added to all requests for that route.  This is done so we don't have to
		//figure out which filters to apply on each request and on startup can wire up all filters once
		router.addFilter("/secure/.*", LoginFilter.class, new LoginInfo(HttpsController.LOGIN_TOKEN, HttpsRouteId.LOGIN), PortType.HTTPS_FILTER);
		router.addFilter("/backend/.*", LoginFilter.class, new LoginInfo("mgrId", HttpsRouteId.LOGIN_BACKEND), PortType.HTTPS_FILTER);
	
		router.addNotFoundFilter(LoginFilter.class, new LoginInfo(HttpsController.LOGIN_TOKEN, HttpsRouteId.LOGIN), PortType.HTTPS_FILTER);
		router.addNotFoundFilter(LoginFilter.class, new LoginInfo("mgrId", HttpsRouteId.LOGIN_BACKEND), PortType.HTTPS_FILTER);

		router.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		router.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
