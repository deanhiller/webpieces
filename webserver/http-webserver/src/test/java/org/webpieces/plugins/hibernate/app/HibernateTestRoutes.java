package org.webpieces.plugins.hibernate.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.ADD_USER_PAGE;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.CONFIRM_DELETE_USER;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.DELETE_USER;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.EDIT_USER_PAGE;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.LIST_USERS;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.SAVE_USER;
import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Routes;

public class HibernateTestRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		CrudRouteIds routeIds = new CrudRouteIds(LIST_USERS, ADD_USER_PAGE, EDIT_USER_PAGE, SAVE_USER, CONFIRM_DELETE_USER, DELETE_USER);
		bldr.addCrud(BOTH, "user", "CrudTestController", routeIds);
		
		bldr.addRoute(BOTH, GET , "/multiselect/{id}", "CrudTestController.multiSelect", HibernateRouteId.MULTISELECT);
		bldr.addRoute(BOTH, POST, "/multiselect", "CrudTestController.postSaveUserForMultiSelect", HibernateRouteId.POST_MULTISELECT);
		
		bldr.addRoute(BOTH, POST, "/save",          "HibernateController.save", HibernateRouteId.SAVE_ENTITY);
		bldr.addRoute(BOTH, GET , "/get/{id}",      "HibernateController.display", HibernateRouteId.DISPLAY_ENTITY);
		bldr.addRoute(BOTH, GET , "/dynamic/{id}",  "HibernateController.entityLoad", HibernateRouteId.ENTITY_LOAD);
		
		bldr.addRoute(BOTH, POST, "/testmerge",     "HibernateController.postMergeUserTest", HibernateRouteId.MERGE_ENTITY);
		
		bldr.addRoute(BOTH, POST, "/async/save",        "HibernateAsyncController.save", HibernateRouteId.ASYNC_SAVE_ENTITY);
		bldr.addRoute(BOTH, GET , "/async/get/{id}",    "HibernateAsyncController.display", HibernateRouteId.ASYNC_DISPLAY_ENTITY);
		bldr.addRoute(BOTH, GET , "/async/dynamic/{id}","HibernateAsyncController.entityLoad", HibernateRouteId.ASYNC_ENTITY_LOAD);

		bldr.addRoute(BOTH, GET , "/fail",          "HibernateController.saveThenFail", HibernateRouteId.ROLLBACK);
		
		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
