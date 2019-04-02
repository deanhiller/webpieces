package org.webpieces.router.impl.loader.svc;

import org.webpieces.ctx.api.RequestContext;

public class MethodMeta {

	private RequestContext ctx;
	private RouteInfo route;
	private LoadedController2 loadedController;

	public MethodMeta(LoadedController2 loadedController, RequestContext ctx, RouteInfo route) {
		this.loadedController = loadedController;
		this.ctx = ctx;
		this.route = route;
	}

	public RequestContext getCtx() {
		return ctx;
	}

	public RouteInfo getRoute() {
		return route;
	}

	public LoadedController2 getLoadedController2() {
		return loadedController;
	}

}
