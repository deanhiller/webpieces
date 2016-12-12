package org.webpieces.webserver.basic.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class BasicRouteModule extends AbstractRouteModule {

	@Override
	protected void configure(String currentPackage) {
		addRoute(GET , "/",                  "biz/BasicController.someMethod", BasicRouteId.SOME_ROUTE);
		addRoute(GET , "/redirect/{id}",     "biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		addRoute(GET , "/redirectint/{id}",  "biz/BasicController.redirectWithInt", BasicRouteId.REDIRECT2);
		addRoute(GET , "/myroute",           "biz/BasicController.myMethod", BasicRouteId.RENDER_PAGE);
		addRoute(GET , "/throwNotFound",     "biz/BasicController.throwNotFound", BasicRouteId.THROW_NOT_FOUND);
		addRoute(GET , "/badtemplate",       "biz/BasicController.badTemplate", BasicRouteId.BAD_TEMPLATE);
		addRoute(GET , "/somejson",          "biz/BasicController.jsonFile", BasicRouteId.JSON_ROUTE);
		addRoute(GET , "/rawurlredirect",    "biz/BasicController.redirectRawUrl", BasicRouteId.REDIRECT_RAW_URL);
		addRoute(GET , "/rawabsoluteurlredirect",    "biz/BasicController.redirectRawAbsoluteUrl", BasicRouteId.REDIRECT_ABSOLUTE_URL);

		addRoute(GET , "/null",              "biz/BasicController.returnNull", BasicRouteId.NULL_ROUTE);

		// #{form action:@POST_CATCH_ALL[:] id:'detailsForm'}#  ....   #{/form}#
		//addPostRoute("/post/{_controller}/{_action}", "{_controller}.post{_action}", BasicRouteId.POST_CATCH_ALL); //catch all post route
		
		setPageNotFoundRoute("biz/BasicController.notFound");
		setInternalErrorRoute("biz/BasicController.internalError");
	}

}
