package org.webpieces.router.impl.routeinvoker;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;

public class InvokeInfo {
	private final BaseRouteInfo route;
	private final RequestContext requestCtx;
	private ProxyStreamHandle handler;
	
	/**
	 * @deprecated Use routeType == RouteType.CONTENT instead!!
	 */
	@Deprecated
	private boolean hasBodyContentBinder;

	public InvokeInfo(BaseRouteInfo route, RequestContext ctx, ProxyStreamHandle handler, boolean hasBodyContentBinder) {
		this.route = route;
		this.requestCtx = ctx;
		this.handler = handler;
		this.hasBodyContentBinder = hasBodyContentBinder;
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

	/**
	 * @deprecated Use routeType == RouteType.CONTENT instead!!
	 */
	@Deprecated
	public boolean isHasBodyContentBinder() {
		return hasBodyContentBinder;
	}
}
