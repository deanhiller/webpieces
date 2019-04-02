package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.util.filters.Service;

public class InternalErrorRouter {

	private final RouteInvoker2 invoker;
	private final BaseRouteInfo route;
	private LoadedController loadedController;
	private Service<MethodMeta, Action> svc;

	public InternalErrorRouter(RouteInvoker2 invoker, BaseRouteInfo route,
			LoadedController loadedController, Service<MethodMeta, Action> svc) {
		this.invoker = invoker;
		this.route = route;
		this.loadedController = loadedController;
		this.svc = svc;
	}

	public CompletableFuture<Void> invokeErrorRoute(RequestContext ctx, ResponseStreamer responseCb) {
		DynamicInfo info = new DynamicInfo(loadedController, svc);
		return invoker.invokeErrorRoute(route, info, ctx, responseCb);
	}
}
