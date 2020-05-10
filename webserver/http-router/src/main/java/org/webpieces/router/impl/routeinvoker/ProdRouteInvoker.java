package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Provider;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;
import org.webpieces.router.impl.services.RouteInfoForInternalError;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2engine.api.StreamWriter;

public class ProdRouteInvoker extends AbstractRouteInvoker {

	@Inject
	public ProdRouteInvoker(
		ControllerLoader controllerFinder,
		FutureHelper futureUtil,
		WebSettings webSettings,
		BodyParsers bodyParsers,
		Provider<ResponseStreamer> proxyProvider
	) {
		super(controllerFinder, futureUtil, webSettings, bodyParsers, proxyProvider);
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeStatic(RequestContext ctx, ProxyStreamHandle handle, RouteInfoForStatic data) {
		return super.invokeStatic(ctx, handle, data).thenApply(s -> new NullWriter());
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeErrorController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		RouteInfoForInternalError routeData = (RouteInfoForInternalError) data;
		ResponseStreamer proxyResponse = proxyProvider.get();
		proxyResponse.init(invokeInfo.getRequestCtx().getRequest(), invokeInfo.getHandler(), webSettings.getMaxBodySizeToSend());

		ResponseProcessorAppError processor = new ResponseProcessorAppError(
				invokeInfo.getRequestCtx(), dynamicInfo.getLoadedController(), proxyResponse);
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, routeData.isForceEndOfStream());
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseStreamer proxyResponse = proxyProvider.get();
		proxyResponse.init(invokeInfo.getRequestCtx().getRequest(), invokeInfo.getHandler(), webSettings.getMaxBodySizeToSend());

		ResponseProcessorHtml processor = new ResponseProcessorHtml(
				invokeInfo.getRequestCtx(), reverseRoutes, 
				dynamicInfo.getLoadedController(), proxyResponse);
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, false);
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseStreamer proxyResponse = proxyProvider.get();
		proxyResponse.init(invokeInfo.getRequestCtx().getRequest(), invokeInfo.getHandler(), webSettings.getMaxBodySizeToSend());

		RouteInfoForContent content = (RouteInfoForContent) data;
		if(content.getBodyContentBinder() == null)
			throw new IllegalArgumentException("bodyContentBinder is required for these routes yet it is null here.  bug");
		ResponseProcessorContent processor = new ResponseProcessorContent(invokeInfo.getRequestCtx(), proxyResponse);
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, false);
	}
	
	@Override
	public CompletableFuture<StreamWriter> invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data) {
		return super.invokeNotFound(invokeInfo, loadedController, data);
	}

}
