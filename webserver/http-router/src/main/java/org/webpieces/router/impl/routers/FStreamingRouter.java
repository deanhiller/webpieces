package org.webpieces.router.impl.routers;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStream;

public class FStreamingRouter extends AbstractDynamicRouter implements ReversableRouter {

	private final RouteInvoker routeInvoker;

	public FStreamingRouter(RouteInvoker routeInvoker, MatchInfo matchInfo) {
		super(matchInfo);
		this.routeInvoker = routeInvoker;
	}

	@Override
	public RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteData data = new RouteInfoForStream();
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handler, false);
		return routeInvoker.invokeStreamingController(invokeInfo, dynamicInfo, data);	}

	@Override
	public String getFullPath() {
		return matchInfo.getFullPath();
	}

}
