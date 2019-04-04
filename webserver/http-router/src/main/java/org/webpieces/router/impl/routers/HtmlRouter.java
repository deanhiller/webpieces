package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.InvokeInfo;
import org.webpieces.router.impl.RouteInvoker;
import org.webpieces.router.impl.loader.svc.RouteData;
import org.webpieces.router.impl.loader.svc.RouteInfoForHtml;

public class HtmlRouter implements AbstractRouter {
	
	private final RouteInvoker invoker;
	private final MatchInfo matchInfo;
	//private final RouteId routeId;
	private final boolean isCheckSecureToken;
	
	//hmmmm, this was a bit of a pain.  It is only set once but it's hard to design the code to pass in during construction
	//without quite a bit of work
	private BaseRouteInfo baseRouteInfo;
	private DynamicInfo dynamicInfo;


	public HtmlRouter(RouteInvoker invoker, MatchInfo matchInfo, boolean checkToken) {
		this.invoker = invoker;
		this.matchInfo = matchInfo;
		this.isCheckSecureToken = checkToken;
	}

	@Override
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb,
			Map<String, String> pathParams) {
		RouteData data = new RouteInfoForHtml(isCheckSecureToken, matchInfo.getHttpMethod());
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, responseCb);
		return invoker.invokeHtmlController(invokeInfo, dynamicInfo, data);
	}
	
	public void setBaseRouteInfo(BaseRouteInfo baseRouteInfo) {
		this.baseRouteInfo = baseRouteInfo;
	}

	public void setDynamicInfo(DynamicInfo dynamicInfo) {
		this.dynamicInfo = dynamicInfo;
	}

	public MatchInfo getMatchInfo() {
		return matchInfo;
	}

	public String getFullPath() {
		return matchInfo.getFullPath();
	}
}
