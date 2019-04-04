package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.BaseRouteInfo;

public interface AbstractRouter {

	MatchInfo getMatchInfo();

	CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb, Map<String, String> pathParams);

	void setBaseRouteInfo(BaseRouteInfo baseRouteInfo);

	void setDynamicInfo(DynamicInfo dynamicInfo);

}
