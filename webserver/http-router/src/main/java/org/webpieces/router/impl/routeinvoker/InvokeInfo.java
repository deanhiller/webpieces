package org.webpieces.router.impl.routeinvoker;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;

public class InvokeInfo {
	private final BaseRouteInfo route;
	private final RequestContext requestCtx;
	private RouterStreamHandle handler;

	public InvokeInfo(BaseRouteInfo route, RequestContext ctx, RouterStreamHandle handler) {
		this.route = route;
		this.requestCtx = ctx;
		this.handler = handler;
	}
	
	public BaseRouteInfo getRoute() {
		return route;
	}

	public RequestContext getRequestCtx() {
		return requestCtx;
	}

	public RouterStreamHandle getHandler() {
		return handler;
	}
}
