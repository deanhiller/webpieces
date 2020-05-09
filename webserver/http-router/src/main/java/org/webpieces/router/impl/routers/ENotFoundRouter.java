package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamWriter;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForNotFound;

public class ENotFoundRouter {

	private final RouteInvoker invoker;
	private final BaseRouteInfo baseRouteInfo;
	private LoadedController loadedController;

	public ENotFoundRouter(RouteInvoker invoker, BaseRouteInfo route,
			LoadedController loadedController) {
		this.invoker = invoker;
		this.baseRouteInfo = route;
		this.loadedController = loadedController;
	}

	public CompletableFuture<StreamWriter> invokeNotFoundRoute(RequestContext ctx, RouterStreamHandle handle, NotFoundException exc) {
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handle);
		RouteData data = new RouteInfoForNotFound(exc);
		return invoker.invokeNotFound(invokeInfo, loadedController, data);
	}
}
