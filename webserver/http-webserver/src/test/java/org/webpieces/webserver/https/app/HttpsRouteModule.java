package org.webpieces.webserver.https.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.PortType;
import org.webpieces.webserver.api.HttpLoginFilter;
import org.webpieces.webserver.api.LoginInfo;

public class HttpsRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {
		
		addHttpsRoute(GET , "/secureRoute",         "HttpsController.home", HttpsRouteId.HOME);
		addHttpsRoute(GET , "/login",               "HttpsController.login", HttpsRouteId.LOGIN);
		addHttpsRoute(POST, "/postLogin",           "HttpsController.postLogin", HttpsRouteId.POST_LOGIN);
		addHttpsRoute(GET , "/secure/internal",     "HttpsController.internal", HttpsRouteId.INTERNAL);

		//in this case, https route of same path is never hit since the order is wrong...
		addRoute(GET ,     "/same",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE);
		addHttpsRoute(GET, "/same",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE);
		
		//This is the correct order, https://.../same2 shows the https route and http://.../same2 shows the http route...
		addHttpsRoute(GET, "/same2",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE2);		
		addRoute(GET ,     "/same2",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE2);

		//Unlike routes which apply regex to request urls, filters regexs are applied to route regexs so if a filter
		//matches a route, it will be added to all requests for that route.  This is done so we don't have to
		//figure out which filters to apply on each request and on startup can wire up all filters once
		addFilter("/secure/.*", HttpLoginFilter.class, new LoginInfo("userId", HttpsRouteId.LOGIN), PortType.HTTPS_FILTER);
		addFilter("/backend/.*", HttpLoginFilter.class, new LoginInfo("mgrId", HttpsRouteId.LOGIN_BACKEND), PortType.HTTPS_FILTER);
		
		setPageNotFoundRoute("../../basic/biz/BasicController.notFound");
		setInternalErrorRoute("../../basic/biz/BasicController.internalError");
	}

}
