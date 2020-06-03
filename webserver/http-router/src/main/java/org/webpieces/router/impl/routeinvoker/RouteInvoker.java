package org.webpieces.router.impl.routeinvoker;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;

import com.webpieces.http2.api.streaming.StreamWriter;

import java.util.concurrent.CompletableFuture;


public interface RouteInvoker {

	void init(ReverseRoutes reverseRoutes);

	//Even I admit this is a bit ridiculous BUT each one in DevRouteInvoker ONLY is slightly different.  It's very very
	//annoying.  If we were only doing ProdRouteInvoker, then they are all the same except the invokeNotFound and invokeStatic
	//which makes this very annoying!!  I could pass in the function!!!  if I do that, all of this collapses

	CompletableFuture<StreamWriter> invokeErrorController(InvokeInfo invokeInfo, DynamicInfo info, RouteData data);

	RouterStreamRef invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);

	RouterStreamRef invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);

	RouterStreamRef invokeStreamingController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);

	CompletableFuture<StreamWriter> invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data);

	RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data);

}
