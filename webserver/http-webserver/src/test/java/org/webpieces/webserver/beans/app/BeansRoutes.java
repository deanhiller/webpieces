package org.webpieces.webserver.beans.app;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routes.Port.BOTH;

public class BeansRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/pageparam",         "BeansController.pageParam", BeansRouteId.PAGE_PARAM);
		bldr.addRoute(BOTH, GET , "/pageparam_async",   "BeansController.pageParamAsync", BeansRouteId.PAGE_PARAM_ASYNC);

		bldr.addRoute(BOTH, GET , "/urlencoding/{user}","BeansController.urlEncoding", BeansRouteId.URLENCODE);

		bldr.addRoute(BOTH, POST, "/postuser2",        "BeansController.postUser", BeansRouteId.POST_USER2_ROUTE, false); //insecure
		bldr.addRoute(BOTH, POST, "/postArray2",        "BeansController.postArray", BeansRouteId.POST_ARRAY2_ROUTE, false); //insecure

		bldr.addRoute(BOTH, GET , "/adduser",           "BeansController.userForm", BeansRouteId.USER_FORM_ROUTE);
		bldr.addRoute(BOTH, POST, "/postuser",          "BeansController.postUser", BeansRouteId.POST_USER_ROUTE);
		bldr.addRoute(BOTH, GET , "/listusers",         "BeansController.listUsers", BeansRouteId.LIST_USERS_ROUTE);

		bldr.addRoute(BOTH, POST, "/postusernullable",  "BeansController.postUserNullable", BeansRouteId.POST_USER_NULLABLE_ROUTE, false);

		bldr.addRoute(BOTH, GET , "/getuser",           "BeansController.userParamGetRequest", BeansRouteId.USER_GET_ROUTE);

		bldr.addRoute(BOTH, GET , "/arrayForm",         "BeansController.arrayForm", BeansRouteId.ARRAY_FORM_ROUTE);
		bldr.addRoute(BOTH, POST, "/postArray",         "BeansController.postArray", BeansRouteId.POST_ARRAY_ROUTE);
		
		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
