package org.webpieces.webserver.domains.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.PortType;
import org.webpieces.webserver.api.HttpLoginFilter;
import org.webpieces.webserver.api.LoginInfo;

public class DomainsRouteModule extends AbstractRouteModule {

	public static final String LOGIN_TOKEN = "userId";
	@Override
	public void configure() {
		
		addHttpsRoute(GET , "/secureRoute",         "HttpsController.home", DomainsRouteId.HOME);
		addHttpsRoute(GET , "/login",               "HttpsController.login", DomainsRouteId.LOGIN);
		addHttpsRoute(POST, "/postLogin",           "HttpsController.postLogin", DomainsRouteId.POST_LOGIN, false);
		addHttpsRoute(GET , "/secure/internal",     "HttpsController.internal", DomainsRouteId.INTERNAL);

		//in this case, https route of same path is never hit since the order is wrong...
		addRoute(GET ,     "/same",             "HttpsController.httpRoute", DomainsRouteId.HTTP_ROUTE);
		addHttpsRoute(GET, "/same",             "HttpsController.httpsRoute", DomainsRouteId.HTTPS_ROUTE);
		
		//This is the correct order, https://.../same2 shows the https route and http://.../same2 shows the http route...
		addHttpsRoute(GET, "/same2",             "HttpsController.httpsRoute", DomainsRouteId.HTTPS_ROUTE2);		
		addRoute(GET ,     "/same2",             "HttpsController.httpRoute", DomainsRouteId.HTTP_ROUTE2);

		//Unlike routes which apply regex to request urls, filters regexs are applied to route regexs so if a filter
		//matches a route, it will be added to all requests for that route.  This is done so we don't have to
		//figure out which filters to apply on each request and on startup can wire up all filters once
		addFilter("/secure/.*", HttpLoginFilter.class, new LoginInfo(LOGIN_TOKEN, DomainsRouteId.LOGIN), PortType.HTTPS_FILTER);
		addFilter("/backend/.*", HttpLoginFilter.class, new LoginInfo("mgrId", DomainsRouteId.LOGIN_BACKEND), PortType.HTTPS_FILTER);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
