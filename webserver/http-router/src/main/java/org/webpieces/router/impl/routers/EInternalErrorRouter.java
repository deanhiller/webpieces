package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteInfoForInternalError;
import org.webpieces.util.filters.Service;

import com.webpieces.http2engine.api.StreamWriter;

public class EInternalErrorRouter {

	private final RouteInvoker invoker;
	private final BaseRouteInfo baseRouteInfo;
	private LoadedController loadedController;
	private Service<MethodMeta, Action> svc;

	public EInternalErrorRouter(RouteInvoker invoker, BaseRouteInfo route,
			LoadedController loadedController, Service<MethodMeta, Action> svc) {
		this.invoker = invoker;
		this.baseRouteInfo = route;
		this.loadedController = loadedController;
		this.svc = svc;
	}

	public CompletableFuture<StreamWriter> invokeErrorRoute(RequestContext ctx, ProxyStreamHandle handler, boolean forceEndOfStream) {
		DynamicInfo info = new DynamicInfo(loadedController, svc);
		RouteInfoForInternalError data = new RouteInfoForInternalError(forceEndOfStream);
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handler);
		return invoker.invokeErrorController(invokeInfo, info, data);
	}
}
