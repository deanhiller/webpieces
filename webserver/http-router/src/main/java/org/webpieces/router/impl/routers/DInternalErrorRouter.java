package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteInfoForInternalError;
import org.webpieces.util.filters.Service;

public class DInternalErrorRouter {

	private final RouteInvoker invoker;
	private final BaseRouteInfo baseRouteInfo;
	private LoadedController loadedController;
	private Service<MethodMeta, Action> svc;

	public DInternalErrorRouter(RouteInvoker invoker, BaseRouteInfo route,
			LoadedController loadedController, Service<MethodMeta, Action> svc) {
		this.invoker = invoker;
		this.baseRouteInfo = route;
		this.loadedController = loadedController;
		this.svc = svc;
	}

	public CompletableFuture<Void> invokeErrorRoute(RequestContext ctx, ResponseStreamer responseCb) {
		DynamicInfo info = new DynamicInfo(loadedController, svc);
		RouteInfoForInternalError data = new RouteInfoForInternalError();
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, responseCb);
		return invoker.invokeErrorController(invokeInfo, info, data);
	}
}
