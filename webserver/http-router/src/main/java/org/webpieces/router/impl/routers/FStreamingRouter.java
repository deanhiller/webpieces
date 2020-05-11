package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStream;

import com.webpieces.http2engine.api.StreamWriter;

public class FStreamingRouter extends AbstractDynamicRouterImpl implements ReversableRouter {

	private final RouteInvoker routeInvoker;
	private final MatchInfo matchInfo;
	private BaseRouteInfo baseRouteInfo;

	public FStreamingRouter(RouteInvoker routeInvoker, MatchInfo matchInfo) {
		super(matchInfo);
		this.routeInvoker = routeInvoker;
		this.matchInfo = matchInfo;
	}

	@Override
	public MatchInfo getMatchInfo() {
		return matchInfo;
	}
	
	@Override
	public CompletableFuture<StreamWriter> invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteData data = new RouteInfoForStream();
		InvokeInfo invokeInfo = new InvokeInfo(baseRouteInfo, ctx, handler);
		return routeInvoker.invokeStreamingController(invokeInfo, dynamicInfo, data);	}

	@Override
	public void setBaseRouteInfo(BaseRouteInfo baseRouteInfo) {
		this.baseRouteInfo = baseRouteInfo;
	}
	
	@Override
	public String getFullPath() {
		return matchInfo.getFullPath();
	}

}
