package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.streams.StreamService;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.Endpoint;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdRouteInvoker.class)
public interface RouteInvoker {

	//void init(ReverseRoutes reverseRoutes);

	//Even I admit this is a bit ridiculous BUT each one in DevRouteInvoker ONLY is slightly different.  It's very very
	//annoying.  If we were only doing ProdRouteInvoker, then they are all the same except the invokeNotFound and invokeStatic
	//which makes this very annoying!!  I could pass in the function!!!  if I do that, all of this collapses

	CompletableFuture<Void> invokeErrorController(InvokeInfo invokeInfo, Endpoint info, RouteData data);

	RouterStreamRef invokeHtmlController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data);

	RouterStreamRef invokeContentController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data);

	RouterStreamRef invokeStreamingController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data);

	CompletableFuture<Void> invokeNotFound(InvokeInfo invokeInfo, Endpoint info, RouteData data);

	RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data);

}
