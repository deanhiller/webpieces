package org.webpieces.webserver.https.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.Router;
import org.webpieces.webserver.api.login.LoginFilter;
import org.webpieces.webserver.api.login.LoginInfo;

public class HttpsRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		Router httpsRouter = router.getScopedRouter(null, true);
		
		httpsRouter.addRoute(GET , "/secureRoute",         "HttpsController.home", HttpsRouteId.HOME);
		httpsRouter.addRoute(GET , "/login",               "HttpsController.login", HttpsRouteId.LOGIN);
		httpsRouter.addRoute(POST, "/postLogin",           "HttpsController.postLogin", HttpsRouteId.POST_LOGIN, false);
		httpsRouter.addRoute(GET , "/secure/internal",     "HttpsController.internal", HttpsRouteId.INTERNAL);

		//in this case, https route of same path is never hit since the order is wrong...
		addRoute(GET ,     "/same",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE);
		httpsRouter.addRoute(GET, "/same",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE);
		
		//This is the correct order, https://.../same2 shows the https route and http://.../same2 shows the http route...
		httpsRouter.addRoute(GET, "/same2",             "HttpsController.httpsRoute", HttpsRouteId.HTTPS_ROUTE2);		
		addRoute(GET ,     "/same2",             "HttpsController.httpRoute", HttpsRouteId.HTTP_ROUTE2);

		addRoute(GET ,     "/backendlogin",      "HttpsController.httpRoute", HttpsRouteId.LOGIN_BACKEND);

		//Unlike routes which apply regex to request urls, filters regexs are applied to route regexs so if a filter
		//matches a route, it will be added to all requests for that route.  This is done so we don't have to
		//figure out which filters to apply on each request and on startup can wire up all filters once
		addFilter("/secure/.*", LoginFilter.class, new LoginInfo(LoginInfo.LOGIN_TOKEN1, HttpsRouteId.LOGIN), PortType.HTTPS_FILTER);
		addFilter("/backend/.*", LoginFilter.class, new LoginInfo("mgrId", HttpsRouteId.LOGIN_BACKEND), PortType.HTTPS_FILTER);
	
		addNotFoundFilter(LoginFilter.class, new LoginInfo(LoginInfo.LOGIN_TOKEN1, HttpsRouteId.LOGIN), PortType.HTTPS_FILTER);
		addNotFoundFilter(LoginFilter.class, new LoginInfo("mgrId", HttpsRouteId.LOGIN_BACKEND), PortType.HTTPS_FILTER);

		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
