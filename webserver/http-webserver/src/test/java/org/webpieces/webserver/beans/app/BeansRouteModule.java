package org.webpieces.webserver.beans.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class BeansRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {
		addRoute(GET , "/pageparam",         "BeansController.pageParam", BeansRouteId.PAGE_PARAM);

		addRoute(GET , "/urlencoding/{user}","BeansController.urlEncoding", BeansRouteId.URLENCODE);
		
		addRoute(GET , "/adduser",           "BeansController.userForm", BeansRouteId.USER_FORM_ROUTE);
		addRoute(POST, "/postuser",          "BeansController.postUser", BeansRouteId.POST_USER_ROUTE);
		addRoute(GET , "/listusers",         "BeansController.listUsers", BeansRouteId.LIST_USERS_ROUTE);
		
		addRoute(GET , "/arrayForm",         "BeansController.arrayForm", BeansRouteId.ARRAY_FORM_ROUTE);
		addRoute(POST, "/postArray",         "BeansController.postArray", BeansRouteId.POST_ARRAY_ROUTE);
		
		setPageNotFoundRoute("../../basic/biz/BasicController.notFound");
		setInternalErrorRoute("../../basic/biz/BasicController.internalError");
	}

}
