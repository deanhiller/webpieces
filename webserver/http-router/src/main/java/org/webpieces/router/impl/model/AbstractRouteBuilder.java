package org.webpieces.router.impl.model;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.CrudRouteIds;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteImpl;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public abstract class AbstractRouteBuilder implements Router {

	private static final Logger log = LoggerFactory.getLogger(AbstractRouteBuilder.class);

	public static ThreadLocal<RouteModuleInfo> currentPackage = new ThreadLocal<>();

	protected L3PrefixedRouting routes;
	protected RouterInfo routerInfo;
	protected LogicHolder holder;

	protected boolean isHttpsOnlyRoutes;

	public AbstractRouteBuilder(RouterInfo routerInfo, L3PrefixedRouting routes, LogicHolder holder, boolean isHttpsOnlyRoutes) {
		this.routerInfo = routerInfo;
		this.routes = routes;
		this.holder = holder;
		this.isHttpsOnlyRoutes = isHttpsOnlyRoutes;
	}

	@Override
	public Router getScopedRouter(String path, boolean isHttpsOnlyRoutes) {
		L3PrefixedRouting router = routes;
		if(path != null) {
			if(path.length() <= 1)
				throw new IllegalArgumentException("path must be non-null and size greater than 1");
			else if(!path.startsWith("/"))
				throw new IllegalArgumentException("path must start with /");
			else if(path.endsWith("/"))
				throw new IllegalArgumentException("path must not end with /");
			
			router = routes.getScopedRouter(path);
		} else
			path = "";
			
		RouterInfo info = new RouterInfo(routerInfo.getDomain(), routerInfo.getPath()+path);
		return new R3PrefixRouterBuilder(info, router, holder, isHttpsOnlyRoutes);
	}
	
	public void addRoute(Route r, RouteId routeId) {
		log.info("scope:'"+routerInfo+"' adding route="+r.getFullPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, holder.getInjector(), currentPackage.get(), holder.getUrlEncoding());
		holder.getFinder().loadControllerIntoMetaObject(meta, true);

		routes.addRoute(meta);
		
		holder.getReverseRoutes().addRoute(routeId, meta);
	}

	@Override
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		boolean checkSecureToken = false;
		if(method == HttpMethod.POST)
			checkSecureToken = true;
		addRoute(method, path, controllerMethod, routeId, checkSecureToken);
	}
	
	@Override
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken) {
		UrlPath p = new UrlPath(routerInfo, path);
		Route route = new RouteImpl(method, p, controllerMethod, routeId, isHttpsOnlyRoutes, checkToken);
		addRoute(route, routeId);
	}
	
	/*
	 * Adds routes like the following all in one method
	 * 
	 * 	//addRoute(GET ,   "/user/list",        "crud/CrudUserController.userList", listRoute);
	 *	//addRoute(GET ,   "/user/new",         "crud/CrudUserController.userAddEdit", addRoute);
	 *	//addRoute(GET ,   "/user/edit/{id}",   "crud/CrudUserController.userAddEdit", editRoute);
	 *	//addRoute(POST,   "/user/post",        "crud/CrudUserController.postSaveUser", saveRoute);
	 *	//addRoute(GET,    "/user/delete/{id}", "crud/CrudUserController.postDeleteUser", deleteRoute);
	 */
	@Override
	public void addCrud(String entity, String controller, CrudRouteIds routeIds) {
		RouteId listRoute = routeIds.getListRoute();
		RouteId addRoute = routeIds.getAddRoute();
		RouteId editRoute = routeIds.getEditRoute(); 
		RouteId postSaveRoute = routeIds.getPostSaveRoute();
		RouteId confirmDelete = routeIds.getConfirmDelete();
		RouteId postDeleteRoute = routeIds.getPostDeleteRoute();
		
		String entityWithCapital = entity.substring(0, 1).toUpperCase() + entity.substring(1);
		addRoute(GET , "/"+entity+"/list",        controller+"."+entity+"List", listRoute);
		addRoute(GET , "/"+entity+"/new",         controller+"."+entity+"AddEdit", addRoute);
		addRoute(GET , "/"+entity+"/edit/{id}",   controller+"."+entity+"AddEdit", editRoute);
		addRoute(POST, "/"+entity+"/post",        controller+".postSave"+entityWithCapital, postSaveRoute);

		//get the confirm delete page
		addRoute(GET,  "/"+entity+"/confirmdelete/{id}", controller+".confirmDelete"+entityWithCapital, confirmDelete);
		//NOTE: Browsers don't support DELETE.  POST might make more sense here for delete but GET is way way less html
		//code(ok, 1 line instead of 3).  There are hacks with javascript to support DELETE but seriously, we should just
		//KISS and YAGNI (google that if you don't know).  
		//HOWEVER, If you don't like this, copy and paste this method and modify to be a POST OR DELETE and add the 
		//javascript for next time
		addRoute(POST, "/"+entity+"/delete/{id}", controller+".postDelete"+entityWithCapital, postDeleteRoute);
	}
	
}
