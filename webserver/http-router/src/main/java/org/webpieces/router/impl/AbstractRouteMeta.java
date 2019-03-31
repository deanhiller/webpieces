package org.webpieces.router.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.model.MatchResult2;

public interface AbstractRouteMeta {

	MatchResult2 matches2(RouterRequest request, String subPath);

	CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb, Map<String, String> pathParams);

	String getLoggableString(String paddingElement);

}
