package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForHtml;

import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class FHtmlRouter extends AbstractDynamicRouterImpl implements ReversableRouter {
	
	private final RouteInvoker invoker;
	//private final RouteId routeId;
	private final boolean isCheckSecureToken;
	
	//hmmmm, this was a bit of a pain.  It is only set once but it's hard to design the code to pass in during construction
	//without quite a bit of work
	private BaseRouteInfo baseRouteInfo;

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
	
	public void setBaseRouteInfo(BaseRouteInfo baseRouteInfo) {
		this.baseRouteInfo = baseRouteInfo;
	}

	public MatchInfo getMatchInfo() {
		return matchInfo;
	}

	public String getFullPath() {
		return matchInfo.getFullPath();
	}

}
