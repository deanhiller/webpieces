package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.InvokeInfo;
import org.webpieces.router.impl.RouteInvoker;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.svc.RouteData;
import org.webpieces.router.impl.loader.svc.RouteInfoForNotFound;

public class NotFoundRouter {

	private final RouteInvoker invoker;
	private final BaseRouteInfo baseRouteInfo;
	private LoadedController loadedController;

	public NotFoundRouter(RouteInvoker invoker, BaseRouteInfo route,
			LoadedController loadedController) {
		this.invoker = invoker;
		this.baseRouteInfo = route;
		this.loadedController = loadedController;
	}

	public CompletableFuture<Void> invokeNotFoundRoute(RequestContext ctx, ResponseStreamer responseCb, NotFoundException exc) {
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, responseCb);
		RouteData data = new RouteInfoForNotFound(exc);
		return invoker.invokeNotFound(invokeInfo, loadedController, data);
	}
}
