package org.webpieces.plugins.hibernate.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.plugins.hibernate.app.HibernateRouteId.*;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.RouteId;

public class HibernateTestRouteModule extends AbstractRouteModule {

	@Override
	protected void configure(String currentPackage) {
		addRoute(POST, "/save",          "HibernateController.save", HibernateRouteId.SAVE_ENTITY);
		addRoute(GET , "/get/{id}",      "HibernateController.display", HibernateRouteId.DISPLAY_ENTITY);
		addRoute(GET , "/dynamic/{id}",  "HibernateController.entityLoad", HibernateRouteId.ENTITY_LOAD);
		
		addRoute(POST, "/async/save",        "HibernateAsyncController.save", HibernateRouteId.ASYNC_SAVE_ENTITY);
		addRoute(GET , "/async/get/{id}",    "HibernateAsyncController.display", HibernateRouteId.ASYNC_DISPLAY_ENTITY);
		addRoute(GET , "/async/dynamic/{id}","HibernateAsyncController.entityLoad", HibernateRouteId.ASYNC_ENTITY_LOAD);
		
		//addCrud("user", "CrudController", LIST_USERS, ADD_EDIT_USER_PAGE, SAVE_USER, DELETE_USER);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

	public void addCrud(String entity, String controller,  RouteId listRoute, RouteId addRoute, RouteId saveRoute, RouteId deleteRoute) {
		String entityWithCapital = entity.substring(0, 1).toUpperCase() + entity.substring(1);
		addRoute(GET , "/"+entity+"/list",  controller+"."+entity+"List", listRoute);
		List<String> urls = new ArrayList<>();
		urls.add("/"+entity+"/add");
		urls.add("/"+entity+"/edit/{id}");
		addMultiRoute(GET , urls, controller+"."+entity+"AddEdit", addRoute);
		addRoute(POST, "/post"+entity,     controller+".postSave"+entityWithCapital, saveRoute);
		addRoute(POST, "/"+entity+"/delete/{id}", controller+".postDelete"+entityWithCapital, deleteRoute);
	}

}
