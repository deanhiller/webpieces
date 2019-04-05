package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;

public class ProdRouteInvoker extends AbstractRouteInvoker {

	@Inject
	public ProdRouteInvoker(
		ObjectToParamTranslator reverseTranslator,
		RouterConfig config,
		ControllerLoader controllerFinder
	) {
		super(reverseTranslator, config, controllerFinder);
	}
	
	@Override
	public CompletableFuture<Void> invokeStatic(RequestContext ctx, ResponseStreamer responseCb, RouteInfoForStatic data) {
		return super.invokeStatic(ctx, responseCb, data);
	}
	
	@Override
	public CompletableFuture<Void> invokeErrorController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		return invokeImpl(invokeInfo, dynamicInfo, data);
	}
	
	@Override
	public CompletableFuture<Void> invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		return invokeImpl(invokeInfo, dynamicInfo, data);
	}
	
	@Override
	public CompletableFuture<Void> invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		return invokeImpl(invokeInfo, dynamicInfo, data);
	}
	
	@Override
	public CompletableFuture<Void> invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data) {
		return super.invokeNotFound(invokeInfo, loadedController, data);
	}

}
