package org.webpieces.router.api.routes;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.services.RouteData;

public class MethodMeta {

	private RequestContext ctx;
	private RouteData route;
	private LoadedController loadedController;

	public MethodMeta(LoadedController loadedController, RequestContext ctx, RouteData route) {
		this.loadedController = loadedController;
		this.ctx = ctx;
		this.route = route;
	}

	public RequestContext getCtx() {
		return ctx;
	}

	public RouteData getRoute() {
		return route;
	}

	public LoadedController getLoadedController2() {
		return loadedController;
	}

}
