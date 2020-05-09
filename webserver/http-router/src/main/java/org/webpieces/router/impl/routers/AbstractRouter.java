package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamWriter;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.impl.model.MatchResult2;

public interface AbstractRouter {

	MatchInfo getMatchInfo();

	CompletableFuture<StreamWriter> invoke(RequestContext ctx, RouterStreamHandle handler);

	MatchResult2 matches(RouterRequest request, String subPath);

}
