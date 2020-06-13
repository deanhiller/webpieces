package org.webpieces.router.impl.routers;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;

public class FContentRouter extends AbstractDynamicRouter implements ReversableRouter {

	private final RouteInvoker routeInvoker;
	private final BodyContentBinder bodyContentBinder;

	public FContentRouter(RouteInvoker routeInvoker, MatchInfo matchInfo, BodyContentBinder bodyContentBinder) {
		super(matchInfo);
		this.routeInvoker = routeInvoker;
		this.bodyContentBinder = bodyContentBinder;
	}
	
	@Override
	public RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteData data = new RouteInfoForContent(bodyContentBinder);
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handler, true);
		return routeInvoker.invokeContentController(invokeInfo, dynamicInfo, data);	
	}
	
	@Override
	public String getFullPath() {
		return matchInfo.getFullPath();
	}

}
