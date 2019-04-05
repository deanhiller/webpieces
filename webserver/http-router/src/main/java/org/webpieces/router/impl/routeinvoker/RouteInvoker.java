package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;

public interface RouteInvoker {

	void init(ReverseRoutes reverseRoutes);

	//Even I admit this is a bit ridiculous BUT each one in DevRouteInvoker ONLY is slightly different.  It's very very
	//annoying.  If we were only doing ProdRouteInvoker, then they are all the same except the invokeNotFound and invokeStatic
	//which makes this very annoying!!  I could pass in the function!!!  if I do that, all of this collapses
	
	CompletableFuture<Void> invokeErrorController(InvokeInfo invokeInfo, DynamicInfo info, RouteData data);

	CompletableFuture<Void> invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);

	CompletableFuture<Void> invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);	
	
	CompletableFuture<Void> invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data);

	CompletableFuture<Void> invokeStatic(RequestContext ctx, ResponseStreamer responseCb, RouteInfoForStatic data);

}
