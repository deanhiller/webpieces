package org.webpieces.webserver.tags.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.webserver.basic.app.BasicRouteId;

public class TagRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		addRoute(GET , "/verbatim",          "TagController.htmlEscapingOffTag", TagsRouteId.VERBATIM_ROUTE_ID);

		addRoute(GET , "/if",                "TagController.ifTag", TagsRouteId.IF_ROUTE_ID);
		addRoute(GET , "/else",              "TagController.elseTag", TagsRouteId.ELSE_ROUTE_ID);
		addRoute(GET , "/elseif",            "TagController.elseIfTag", TagsRouteId.ELSEIF);
		addRoute(GET , "/list",              "TagController.listTag", TagsRouteId.LIST);
		addRoute(GET , "/emptyList",         "TagController.emptyListTag", TagsRouteId.EMPTY_LIST);
		
		addRoute(GET , "/setget",            "TagController.getTag", TagsRouteId.SETGET);
		addRoute(GET , "/extends",           "TagController.extendsTag", TagsRouteId.EXTENDS);
		addRoute(GET , "/ahref",             "TagController.aHrefTag", TagsRouteId.AHREF);
		//needed for ahref to redirect to
		addRoute(GET , "/redirect/{id}",     "../../basic/app/biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		
		addRoute(GET , "/stylesheet",        "TagController.stylesheetTag", TagsRouteId.STYLESHEET);
		//relative path(to user.dir) for stylesheet / javascript testing
		addStaticDir("/public/", "src/test/resources/staticRoutes/", false);
		
		addRoute(GET , "/bootstrap",         "TagController.bootstrapTag", TagsRouteId.BOOTSTRAP);
		addRoute(GET , "/user/{id}",         "TagController.fakeAjaxAddEditRoute", TagsRouteId.FAKE_ROUTE_ID);
		
		
		addRoute(GET , "/customtag",         "../include/IncludeTagController.customTag", TagsRouteId.CUSTOM_TAG);
		addRoute(GET , "/renderTagArgs",     "../include/IncludeTagController.renderTagArgsTag", TagsRouteId.RENDER_TAG_ARGS_TAG);
		addRoute(GET , "/renderPageArgs",    "../include/IncludeTagController.renderPageArgsTag", TagsRouteId.RENDER_PAGE_ARGS_TAG);

		addRoute(GET , "/customFieldTag",    "../field/FieldTagController.customFieldTag", TagsRouteId.FIELD_TAG);

		addRoute(GET , "/getuserform",       "TagController.formTag", TagsRouteId.GET_USER_FORM);
		addRoute(POST, "/postuser",          "TagController.postSomething", TagsRouteId.POST_USER);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
