package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

import com.webpieces.http2engine.api.StreamWriter;

public interface AbstractRouter {

	MatchInfo getMatchInfo();

	CompletableFuture<StreamWriter> invoke(RequestContext ctx, ProxyStreamHandle handler);

	MatchResult2 matches(RouterRequest request, String subPath);

}
