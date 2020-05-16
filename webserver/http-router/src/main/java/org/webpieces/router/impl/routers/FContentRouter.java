package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;

import com.webpieces.http2engine.api.StreamWriter;

public class FContentRouter extends AbstractDynamicRouterImpl implements ReversableRouter {

	private final RouteInvoker routeInvoker;
	private final MatchInfo matchInfo;
	private final BodyContentBinder bodyContentBinder;

	private BaseRouteInfo baseRouteInfo;

	public FContentRouter(RouteInvoker routeInvoker, MatchInfo matchInfo, BodyContentBinder bodyContentBinder) {
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
	public CompletableFuture<StreamWriter> invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteData data = new RouteInfoForContent(bodyContentBinder);
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handler, true);
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
