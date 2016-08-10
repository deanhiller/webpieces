package org.webpieces.webserver.basic;

import static org.webpieces.router.api.dto.HttpMethod.*;
import org.webpieces.router.api.routing.AbstractRouteModule;

public class BasicRouteModule extends AbstractRouteModule {

	@Override
	protected void configure(String currentPackage) {
		addRoute(GET , "/",                  "biz/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		addRoute(GET , "/redirect/{id}",     "biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		addRoute(GET , "/redirectint/{id}",  "biz/BasicController.redirectWithInt", BasicRouteId.REDIRECT2);
		addRoute(GET , "/myroute",           "biz/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		addRoute(GET , "/throwNotFound",     "biz/BasicController.throwNotFound", BasicRouteId.THROW_NOT_FOUND);
		addRoute(GET , "/pageparam",         "biz/BasicController.pageParam", BasicRouteId.PAGE_PARAM);
		addRoute(GET , "/verbatim",          "biz/BasicController.verbatimTag", BasicRouteId.VERBATIM);

		addRoute(GET , "/if",                "biz/BasicController.ifTag", BasicRouteId.IF);
		addRoute(GET , "/else",              "biz/BasicController.elseTag", BasicRouteId.ELSE);
		addRoute(GET , "/elseif",            "biz/BasicController.elseIfTag", BasicRouteId.ELSEIF);

		addRoute(GET , "/setget",            "biz/BasicController.getTag", BasicRouteId.SETGET);
		addRoute(GET , "/extends",           "biz/BasicController.extendsTag", BasicRouteId.EXTENDS);
		addRoute(GET , "/ahref",             "biz/BasicController.aHrefTag", BasicRouteId.AHREF);
		
		addRoute(GET , "/customtag",         "includetags/IncludeTagController.customTag", BasicRouteId.CUSTOM_TAG);
		addRoute(GET , "/renderTagArgs",     "includetags/IncludeTagController.renderTagArgsTag", BasicRouteId.RENDER_TAG_ARGS_TAG);
		addRoute(GET , "/renderPageArgs",    "includetags/IncludeTagController.renderPageArgsTag", BasicRouteId.RENDER_PAGE_ARGS_TAG);
		
		addRoute(GET , "/urlencoding/{user}","biz/BasicController.urlEncoding", BasicRouteId.URLENCODE);

		addRoute(GET , "/getuserform",       "biz/BasicController.formTag", BasicRouteId.GET_USER_FORM);
		addRoute(POST, "/postuser",          "biz/BasicController.postSomething", BasicRouteId.POST_USER);

		// #{form action:@POST_CATCH_ALL[:] id:'detailsForm'}#  ....   #{/form}#
		//addPostRoute("/post/{_controller}/{_action}", "{_controller}.post{_action}", BasicRouteId.POST_CATCH_ALL); //catch all post route
		
		setPageNotFoundRoute("biz/BasicController.notFound");
		setInternalErrorRoute("biz/BasicController.internalError");
	}

}
