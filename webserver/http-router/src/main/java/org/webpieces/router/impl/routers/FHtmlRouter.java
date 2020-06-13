package org.webpieces.router.impl.routers;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForHtml;

public class FHtmlRouter extends AbstractDynamicRouter implements ReversableRouter {
	
	private final RouteInvoker invoker;
	//private final RouteId routeId;
	private final boolean isCheckSecureToken;
	
	public FHtmlRouter(RouteInvoker invoker, MatchInfo matchInfo, boolean checkToken) {
		super(matchInfo);
		this.invoker = invoker;
		this.isCheckSecureToken = checkToken;
	}

	@Override
	public RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteData data = new RouteInfoForHtml(isCheckSecureToken, matchInfo.getHttpMethod());
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handler, false);
		return invoker.invokeHtmlController(invokeInfo, dynamicInfo, data);
	}
	
	public String getFullPath() {
		return matchInfo.getFullPath();
	}

}
