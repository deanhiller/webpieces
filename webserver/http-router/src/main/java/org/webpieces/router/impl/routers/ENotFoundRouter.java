package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.NullWriter;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteInfoForNotFound;
import org.webpieces.util.filters.Service;

import com.webpieces.http2.api.streaming.StreamWriter;

public class ENotFoundRouter {

	private final RouteInvoker invoker;
	private LoadedController loadedController;
	private Service<MethodMeta, Action> svc;
	private String i18nBaseBundle;

	public ENotFoundRouter(RouteInvoker invoker, String i18nBaseBundle,
			LoadedController loadedController, Service<MethodMeta, Action> svc) {
		this.invoker = invoker;
		this.i18nBaseBundle = i18nBaseBundle;
		this.loadedController = loadedController;
		this.svc = svc;
	}

	public CompletableFuture<StreamWriter> invokeNotFoundRoute(RequestContext ctx, ProxyStreamHandle handle, NotFoundException exc) {
		Endpoint info = new Endpoint(svc);
		RouteInfoForNotFound data = new RouteInfoForNotFound(exc);
		InvokeInfo invokeInfo = new InvokeInfo(ctx, handle, RouteType.NOT_FOUND, loadedController, i18nBaseBundle);
		return invoker.invokeNotFound(invokeInfo, info, data).thenApply(voidd -> new NullWriter());
	}
}
