package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.routers.NullStreamWriter;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;
import org.webpieces.router.impl.services.RouteInfoForInternalError;
import org.webpieces.router.impl.services.RouteInfoForStatic;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;

public class ProdRouteInvoker extends AbstractRouteInvoker {

	@Inject
	public ProdRouteInvoker(
		ControllerLoader controllerFinder,
		FutureHelper futureUtil,
		RouteInvokerStatic staticInvoker,
		BodyParsers bodyParsers
	) {
		super(controllerFinder, futureUtil, staticInvoker, bodyParsers);
	}
	
	@Override
	public RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handle, RouteInfoForStatic data) {
		return super.invokeStatic(ctx, handle, data);
	}
	
	@Override
	public RouterStreamRef invokeErrorController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		RouteInfoForInternalError routeData = (RouteInfoForInternalError) data;

		ResponseProcessorAppError processor = new ResponseProcessorAppError(
				invokeInfo.getRequestCtx(), dynamicInfo.getLoadedController(), invokeInfo.getHandler());
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, routeData.isForceEndOfStream());
	}
	
	@Override
	public RouterStreamRef invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		ResponseProcessorHtml processor = new ResponseProcessorHtml(
				invokeInfo.getRequestCtx(), 
				dynamicInfo.getLoadedController(), invokeInfo.getHandler());
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, false);
	}
	
	@Override
	public RouterStreamRef invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		RouteInfoForContent content = (RouteInfoForContent) data;
		if(content.getBodyContentBinder() == null)
			throw new IllegalArgumentException("bodyContentBinder is required for these routes yet it is null here.  bug");
		ResponseProcessorContent processor = new ResponseProcessorContent(invokeInfo.getRequestCtx(), invokeInfo.getHandler());
		return invokeImpl(invokeInfo, dynamicInfo, data, processor, false);
	}

	@Override
	public RouterStreamRef invokeStreamingController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data) {
		return super.invokeStreamingController(invokeInfo, dynamicInfo, data);
		
	}
	
	@Override
	public RouterStreamRef invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data) {
		return super.invokeNotFound(invokeInfo, loadedController, data);
	}

}
