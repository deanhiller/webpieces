package org.webpieces.router.impl;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;

public class StaticRouteMeta extends AbstractRouteMetaImpl {

	private StaticRoute route;

	public StaticRouteMeta(StaticRoute route, Charset urlEncoding) {
		super(route, urlEncoding);
		this.route = route;
	}

	@Override
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb,
			Map<String, String> pathParams) {
		return route.invokeStatic(pathParams, ctx, responseCb);
	}

}
