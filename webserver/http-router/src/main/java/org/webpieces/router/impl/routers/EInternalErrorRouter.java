package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteInfoForInternalError;
import org.webpieces.util.filters.Service;

import com.webpieces.http2.api.streaming.StreamWriter;

public class EInternalErrorRouter {

	private final RouteInvoker invoker;
	private LoadedController loadedController;
	private Service<MethodMeta, Action> svc;
	private String i18nBundleName;

	public EInternalErrorRouter(RouteInvoker invoker, String i18nBundleName,
			LoadedController loadedController, Service<MethodMeta, Action> svc) {
		this.invoker = invoker;
		this.i18nBundleName = i18nBundleName;
		this.loadedController = loadedController;
		this.svc = svc;
	}

	public CompletableFuture<StreamWriter> invokeErrorRoute(RequestContext ctx, ProxyStreamHandle handle, Throwable exc) {
		Endpoint info = new Endpoint(svc);
		RouteInfoForInternalError data = new RouteInfoForInternalError(exc);
		InvokeInfo invokeInfo = new InvokeInfo(ctx, handle, RouteType.INTERNAL_SERVER_ERROR, loadedController, i18nBundleName);
		return invoker.invokeErrorController(invokeInfo, info, data).thenApply( voidd -> new NullStreamWriter());
	}
}
