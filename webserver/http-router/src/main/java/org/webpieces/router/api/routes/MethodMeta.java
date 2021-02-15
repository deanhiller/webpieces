package org.webpieces.router.api.routes;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.services.RouteData;

public class MethodMeta {

	private RequestContext ctx;
	private RouteData route;
	private LoadedController loadedController;
	private RouteType routeType;

	public MethodMeta(LoadedController loadedController, RequestContext ctx, RouteType routeType, RouteData route) {
		this.loadedController = loadedController;
		this.ctx = ctx;
		this.routeType = routeType;
		this.route = route;
	}

	public RequestContext getCtx() {
		return ctx;
	}

	public RouteData getRoute() {
		return route;
	}

	public LoadedController getLoadedController() {
		return loadedController;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public void setCtx(RequestContext ctx) {
		this.ctx = ctx;
	}

	public void setRoute(RouteData route) {
		this.route = route;
	}

	public void setLoadedController(LoadedController loadedController) {
		this.loadedController = loadedController;
	}

	public void setRouteType(RouteType routeType) {
		this.routeType = routeType;
	}

}
