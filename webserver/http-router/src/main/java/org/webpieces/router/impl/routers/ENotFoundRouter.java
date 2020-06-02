package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForNotFound;

import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;

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

	public CompletableFuture<StreamWriter> invokeNotFoundRoute(RequestContext ctx, ProxyStreamHandle handle, NotFoundException exc) {
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handle, false);
		RouteData data = new RouteInfoForNotFound(exc);
		return invoker.invokeNotFound(invokeInfo, loadedController, data);
	}
}
