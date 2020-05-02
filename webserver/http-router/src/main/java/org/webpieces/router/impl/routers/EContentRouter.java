package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;

public class EContentRouter extends AbstractDynamicRouterImpl implements ReversableRouter {

	private final RouteInvoker routeInvoker;
	private final MatchInfo matchInfo;
	private final BodyContentBinder bodyContentBinder;

	private BaseRouteInfo baseRouteInfo;

	public EContentRouter(RouteInvoker routeInvoker, MatchInfo matchInfo, BodyContentBinder bodyContentBinder) {
		super(matchInfo);
		this.routeInvoker = routeInvoker;
		this.matchInfo = matchInfo;
		this.bodyContentBinder = bodyContentBinder;
	}

	@Override
	public MatchInfo getMatchInfo() {
		return matchInfo;
	}
	
	@Override
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb) {
		RouteData data = new RouteInfoForContent(bodyContentBinder);
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, responseCb);
		return routeInvoker.invokeContentController(invokeInfo, dynamicInfo, data);	}

	@Override
	public void setBaseRouteInfo(BaseRouteInfo baseRouteInfo) {
		this.baseRouteInfo = baseRouteInfo;
	}
	
	@Override
	public String getFullPath() {
		return matchInfo.getFullPath();
	}

}
