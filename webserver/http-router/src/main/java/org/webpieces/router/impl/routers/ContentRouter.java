package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.InvokeInfo;
import org.webpieces.router.impl.RouteInvoker;
import org.webpieces.router.impl.loader.svc.RouteData;
import org.webpieces.router.impl.loader.svc.RouteInfoForContent;
import org.webpieces.router.impl.loader.svc.RouteInfoForHtml;

public class ContentRouter implements AbstractRouter {

	private final RouteInvoker routeInvoker;
	private final MatchInfo matchInfo;
	private final BodyContentBinder bodyContentBinder;

	private BaseRouteInfo baseRouteInfo;
	private DynamicInfo dynamicInfo;

	public ContentRouter(RouteInvoker routeInvoker, MatchInfo matchInfo, BodyContentBinder bodyContentBinder) {
		this.routeInvoker = routeInvoker;
		this.matchInfo = matchInfo;
		this.bodyContentBinder = bodyContentBinder;
	}

	@Override
	public MatchInfo getMatchInfo() {
		return matchInfo;
	}
	
	@Override
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb,
			Map<String, String> pathParams) {
		RouteData data = new RouteInfoForContent(bodyContentBinder);
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, responseCb);
		return routeInvoker.invokeContentController(invokeInfo, dynamicInfo, data);	}

	@Override
	public void setBaseRouteInfo(BaseRouteInfo baseRouteInfo) {
		this.baseRouteInfo = baseRouteInfo;
	}

	@Override
	public void setDynamicInfo(DynamicInfo dynamicInfo) {
		this.dynamicInfo = dynamicInfo;
	}



}
