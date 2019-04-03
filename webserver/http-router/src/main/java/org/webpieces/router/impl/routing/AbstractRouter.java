package org.webpieces.router.impl.routing;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.model.MatchResult2;

public interface AbstractRouter {

	String getFullPath();

	Port getExposedPorts();

	MatchResult2 matches2(RouterRequest request, String subPath);

	CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb, Map<String, String> pathParams);

}
