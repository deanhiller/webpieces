package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.InvokeInfo;
import org.webpieces.router.impl.RouteInvoker;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.router.impl.loader.svc.RouteInfoForInternalError;
import org.webpieces.util.filters.Service;

public class InternalErrorRouter {

	private final RouteInvoker invoker;
	private final BaseRouteInfo baseRouteInfo;
	private LoadedController loadedController;
	private Service<MethodMeta, Action> svc;

	public InternalErrorRouter(RouteInvoker invoker, BaseRouteInfo route,
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
