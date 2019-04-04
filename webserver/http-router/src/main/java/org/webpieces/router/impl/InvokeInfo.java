package org.webpieces.router.impl;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;

public class InvokeInfo {
	private final BaseRouteInfo route;
	private final RequestContext requestCtx;
	private final ResponseStreamer responseCb;

	public InvokeInfo(BaseRouteInfo route, RequestContext ctx, ResponseStreamer responseCb) {
		this.route = route;
		this.requestCtx = ctx;
		this.responseCb = responseCb;
	}
	
	public BaseRouteInfo getRoute() {
		return route;
	}

	public RequestContext getRequestCtx() {
		return requestCtx;
	}

	public ResponseStreamer getResponseCb() {
		return responseCb;
	}
	
}
