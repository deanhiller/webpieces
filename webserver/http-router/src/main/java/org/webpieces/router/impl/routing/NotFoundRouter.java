package org.webpieces.router.impl.routing;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.loader.LoadedController;

public class NotFoundRouter {

	private final RouteInvoker2 invoker;
	private final BaseRouteInfo route;
	private LoadedController loadedController;

	public NotFoundRouter(RouteInvoker2 invoker, BaseRouteInfo route,
			LoadedController loadedController) {
		this.invoker = invoker;
		this.route = route;
		this.loadedController = loadedController;
	}

	public CompletableFuture<Void> invokeNotFoundRoute(RequestContext ctx, ResponseStreamer responseCb, NotFoundException exc) {
		return invoker.invokeNotFound(route, loadedController, ctx, responseCb, exc);
	}
}
