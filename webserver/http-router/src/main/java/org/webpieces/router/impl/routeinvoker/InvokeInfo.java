package org.webpieces.router.impl.routeinvoker;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;

public class InvokeInfo {
	private final BaseRouteInfo route;
	private final RequestContext requestCtx;
	private ProxyStreamHandle handler;

	public InvokeInfo(BaseRouteInfo route, RequestContext ctx, ProxyStreamHandle handler) {
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

	public ProxyStreamHandle getHandler() {
		return handler;
	}
}
