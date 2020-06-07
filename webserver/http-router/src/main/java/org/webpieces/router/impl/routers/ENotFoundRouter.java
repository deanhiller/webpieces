package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForInternalError;
import org.webpieces.router.impl.services.RouteInfoForNotFound;
import org.webpieces.util.filters.Service;

import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class ENotFoundRouter {

	private final RouteInvoker invoker;
	private final BaseRouteInfo baseRouteInfo;
	private LoadedController loadedController;
	private Service<MethodMeta, Action> svc;

	public ENotFoundRouter(RouteInvoker invoker, BaseRouteInfo route,
			LoadedController loadedController, Service<MethodMeta, Action> svc) {
		this.invoker = invoker;
		this.baseRouteInfo = route;
		this.loadedController = loadedController;
		this.svc = svc;
	}

	public CompletableFuture<StreamWriter> invokeNotFoundRoute(RequestContext ctx, ProxyStreamHandle handle, NotFoundException exc) {
		DynamicInfo info = new DynamicInfo(loadedController, svc);
		RouteInfoForNotFound data = new RouteInfoForNotFound(exc);
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handle, false);
		return invoker.invokeNotFound(invokeInfo, info, data);
	}
}
