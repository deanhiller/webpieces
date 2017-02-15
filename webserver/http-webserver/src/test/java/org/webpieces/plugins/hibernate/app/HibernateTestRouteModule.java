package org.webpieces.plugins.hibernate.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.ADD_USER_PAGE;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.CONFIRM_DELETE_USER;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.DELETE_USER;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.EDIT_USER_PAGE;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.LIST_USERS;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.SAVE_USER;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.CrudRouteIds;

public class HibernateTestRouteModule extends AbstractRouteModule {

	@Override
	protected void configure() {
		addRoute(POST, "/save",          "HibernateController.save", HibernateRouteId.SAVE_ENTITY);
		addRoute(GET , "/get/{id}",      "HibernateController.display", HibernateRouteId.DISPLAY_ENTITY);
		addRoute(GET , "/dynamic/{id}",  "HibernateController.entityLoad", HibernateRouteId.ENTITY_LOAD);
		
		addRoute(POST, "/testmerge",     "HibernateController.postMergeUserTest", HibernateRouteId.MERGE_ENTITY);
		
		addRoute(POST, "/async/save",        "HibernateAsyncController.save", HibernateRouteId.ASYNC_SAVE_ENTITY);
		addRoute(GET , "/async/get/{id}",    "HibernateAsyncController.display", HibernateRouteId.ASYNC_DISPLAY_ENTITY);
		addRoute(GET , "/async/dynamic/{id}","HibernateAsyncController.entityLoad", HibernateRouteId.ASYNC_ENTITY_LOAD);

		addRoute(GET , "/fail",          "HibernateController.saveThenFail", HibernateRouteId.ROLLBACK);

		CrudRouteIds routeIds = new CrudRouteIds(LIST_USERS, ADD_USER_PAGE, EDIT_USER_PAGE, SAVE_USER, CONFIRM_DELETE_USER, DELETE_USER);
		addCrud("user", "CrudTestController", routeIds);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
