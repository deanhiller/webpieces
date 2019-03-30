package org.webpieces.webserver.tags.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.webserver.basic.app.BasicRouteId;

public class TagRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();

		bldr.addRoute(BOTH, GET , "/if",                "TagController.ifTag", TagsRouteId.IF_ROUTE_ID);
		bldr.addRoute(BOTH, GET , "/else",              "TagController.elseTag", TagsRouteId.ELSE_ROUTE_ID);
		bldr.addRoute(BOTH, GET , "/elseif",            "TagController.elseIfTag", TagsRouteId.ELSEIF);
		bldr.addRoute(BOTH, GET , "/list",              "TagController.listTag", TagsRouteId.LIST);
		bldr.addRoute(BOTH, GET , "/emptyList",         "TagController.emptyListTag", TagsRouteId.EMPTY_LIST);
		
		bldr.addRoute(BOTH, GET , "/setget",            "TagController.getTag", TagsRouteId.SETGET);
		bldr.addRoute(BOTH, GET , "/extends",           "TagController.extendsTag", TagsRouteId.EXTENDS);
		bldr.addRoute(BOTH, GET , "/ahref",             "TagController.aHrefTag", TagsRouteId.AHREF);
		//needed for ahref to redirect to
		bldr.addRoute(BOTH, GET , "/redirect/{id}",     "../../basic/app/biz/BasicController.redirect", BasicRouteId.REDIRECT_PAGE);
		
		bldr.addRoute(BOTH, GET , "/stylesheet",        "TagController.stylesheetTag", TagsRouteId.STYLESHEET);
		//relative path(to user.dir) for stylesheet / javascript testing
		bldr.addStaticDir(BOTH, "/public/", "src/test/resources/staticRoutes/", false);
		
		bldr.addRoute(BOTH, GET , "/bootstrap",         "TagController.bootstrapTag", TagsRouteId.BOOTSTRAP);
		bldr.addRoute(BOTH, GET , "/user/{id}",         "TagController.fakeAjaxAddEditRoute", TagsRouteId.FAKE_ROUTE_ID);
		
		
		bldr.addRoute(BOTH, GET , "/customtag",         "../include/IncludeTagController.customTag", TagsRouteId.CUSTOM_TAG);
		bldr.addRoute(BOTH, GET , "/renderTagArgs",     "../include/IncludeTagController.renderTagArgsTag", TagsRouteId.RENDER_TAG_ARGS_TAG);
		bldr.addRoute(BOTH, GET , "/renderPageArgs",    "../include/IncludeTagController.renderPageArgsTag", TagsRouteId.RENDER_PAGE_ARGS_TAG);

		bldr.addRoute(BOTH, GET , "/customFieldTag",    "../field/FieldTagController.customFieldTag", TagsRouteId.FIELD_TAG);

		bldr.addRoute(BOTH, GET , "/getuserform",       "TagController.formTag", TagsRouteId.GET_USER_FORM);
		bldr.addRoute(BOTH, POST, "/postuser",          "TagController.postSomething", TagsRouteId.POST_USER);
		
		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
