package org.webpieces.webserver.basic;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.AbstractRouteModule;

public class BasicRouteModule extends AbstractRouteModule {

	@Override
	protected void configure(String currentPackage) {
		addRoute(HttpMethod.GET, "/",                  "biz/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		addRoute(HttpMethod.GET, "/redirect/{id}",     "biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		addRoute(HttpMethod.GET, "/redirectint/{id}",  "biz/BasicController.redirectWithInt", BasicRouteId.REDIRECT2);
		addRoute(HttpMethod.GET, "/myroute",           "biz/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		addRoute(HttpMethod.GET, "/throwNotFound",     "biz/BasicController.throwNotFound", BasicRouteId.THROW_NOT_FOUND);
		addRoute(HttpMethod.GET, "/pageparam",         "biz/BasicController.pageParam", BasicRouteId.PAGE_PARAM);
		addRoute(HttpMethod.GET, "/verbatim",          "biz/BasicController.verbatimTag", BasicRouteId.VERBATIM);

		addRoute(HttpMethod.GET, "/if",                "biz/BasicController.ifTag", BasicRouteId.IF);
		addRoute(HttpMethod.GET, "/else",              "biz/BasicController.elseTag", BasicRouteId.ELSE);
		addRoute(HttpMethod.GET, "/elseif",            "biz/BasicController.elseIfTag", BasicRouteId.ELSEIF);

		addRoute(HttpMethod.GET, "/setget",            "biz/BasicController.getTag", BasicRouteId.SETGET);
		addRoute(HttpMethod.GET, "/extends",           "biz/BasicController.extendsTag", BasicRouteId.EXTENDS);
		addRoute(HttpMethod.GET, "/ahref",             "biz/BasicController.aHrefTag", BasicRouteId.AHREF);

		addRoute(HttpMethod.GET, "/urlencoding/{user}","biz/BasicController.urlEncoding", BasicRouteId.URLENCODE);

		setPageNotFoundRoute("biz/BasicController.notFound");
		setInternalErrorRoute("biz/BasicController.internalError");
	}



}
