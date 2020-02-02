package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.PortConfigLookup;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;
import org.webpieces.router.impl.services.RouteInfoForStatic;

public class ProdRouteInvoker extends AbstractRouteInvoker {

	@Inject
	public ProdRouteInvoker(
		ObjectToParamTranslator reverseTranslator,
		ControllerLoader controllerFinder,
		RedirectFormation redirectFormation
	) {
		super(reverseTranslator, controllerFinder, redirectFormation);
	}
	
	@Override
	public CompletableFuture<Void> invokeStatic(RequestContext ctx, ResponseStreamer responseCb, RouteInfoForStatic data) {
		return super.invokeStatic(ctx, responseCb, data);
	}
	
	@Override
	public CompletableFuture<Void> invokeErrorController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseProcessorAppError processor = new ResponseProcessorAppError(
				invokeInfo.getRequestCtx(), dynamicInfo.getLoadedController(), invokeInfo.getResponseCb());
		return invokeImpl(invokeInfo, dynamicInfo, data, processor);
	}
	
	@Override
	public CompletableFuture<Void> invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseProcessorHtml processor = new ResponseProcessorHtml(
				invokeInfo.getRequestCtx(), reverseRoutes, reverseTranslator, 
				dynamicInfo.getLoadedController(), invokeInfo.getResponseCb(), redirectFormation);
		return invokeImpl(invokeInfo, dynamicInfo, data, processor);
	}
	
	@Override
	public CompletableFuture<Void> invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		RouteInfoForContent content = (RouteInfoForContent) data;
		if(content.getBodyContentBinder() == null)
			throw new IllegalArgumentException("bodyContentBinder is required for these routes yet it is null here.  bug");
		ResponseProcessorContent processor = new ResponseProcessorContent(invokeInfo.getRequestCtx(), invokeInfo.getResponseCb());
		return invokeImpl(invokeInfo, dynamicInfo, data, processor);
	}
	
	@Override
	public CompletableFuture<Void> invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data) {
		return super.invokeNotFound(invokeInfo, loadedController, data);
	}

}
