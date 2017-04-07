package org.webpieces.webserver.beans.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRoutes;

public class BeansRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		addRoute(GET , "/pageparam",         "BeansController.pageParam", BeansRouteId.PAGE_PARAM);
		addRoute(GET , "/pageparam_async",   "BeansController.pageParamAsync", BeansRouteId.PAGE_PARAM_ASYNC);

		addRoute(GET , "/urlencoding/{user}","BeansController.urlEncoding", BeansRouteId.URLENCODE);

		addRoute(POST, "/postuser2",        "BeansController.postUser", BeansRouteId.POST_USER2_ROUTE, false); //insecure
		addRoute(POST, "/postArray2",        "BeansController.postArray", BeansRouteId.POST_ARRAY2_ROUTE, false); //insecure

		addRoute(GET , "/adduser",           "BeansController.userForm", BeansRouteId.USER_FORM_ROUTE);
		addRoute(POST, "/postuser",          "BeansController.postUser", BeansRouteId.POST_USER_ROUTE);
		addRoute(GET , "/listusers",         "BeansController.listUsers", BeansRouteId.LIST_USERS_ROUTE);

		addRoute(POST, "/postusernullable",  "BeansController.postUserNullable", BeansRouteId.POST_USER_NULLABLE_ROUTE, false);

		addRoute(GET , "/getuser",           "BeansController.userParamGetRequest", BeansRouteId.USER_GET_ROUTE);

		addRoute(GET , "/arrayForm",         "BeansController.arrayForm", BeansRouteId.ARRAY_FORM_ROUTE);
		addRoute(POST, "/postArray",         "BeansController.postArray", BeansRouteId.POST_ARRAY_ROUTE);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
