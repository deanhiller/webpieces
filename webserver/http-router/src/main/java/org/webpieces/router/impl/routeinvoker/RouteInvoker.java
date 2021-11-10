package org.webpieces.router.impl.routeinvoker;

import org.webpieces.util.futures.XFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.streams.StreamService;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.Endpoint;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdRouteInvoker.class)
public interface RouteInvoker {

	XFuture<Void> invokeErrorController(InvokeInfo invokeInfo, Endpoint info, RouteData data);

	RouterStreamRef invokeHtmlController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data);

	RouterStreamRef invokeContentController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data);

	RouterStreamRef invokeStreamingController(InvokeInfo invokeInfo, StreamService dynamicInfo, RouteData data);

	XFuture<Void> invokeNotFound(InvokeInfo invokeInfo, Endpoint info, RouteData data);

	RouterStreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data);

}
