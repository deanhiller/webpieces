package org.webpieces.router.impl.routeinvoker;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routers.DynamicInfo;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStatic;

import com.webpieces.http2engine.api.StreamRef;

public interface RouteInvoker {

	void init(ReverseRoutes reverseRoutes);

	//Even I admit this is a bit ridiculous BUT each one in DevRouteInvoker ONLY is slightly different.  It's very very
	//annoying.  If we were only doing ProdRouteInvoker, then they are all the same except the invokeNotFound and invokeStatic
	//which makes this very annoying!!  I could pass in the function!!!  if I do that, all of this collapses
	
	StreamRef invokeErrorController(InvokeInfo invokeInfo, DynamicInfo info, RouteData data);

	StreamRef invokeHtmlController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);

	StreamRef invokeContentController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);

	StreamRef invokeStreamingController(InvokeInfo invokeInfo, DynamicInfo dynamicInfo, RouteData data);

	StreamRef invokeNotFound(InvokeInfo invokeInfo, LoadedController loadedController, RouteData data);

	StreamRef invokeStatic(RequestContext ctx, ProxyStreamHandle handler, RouteInfoForStatic data);

}
