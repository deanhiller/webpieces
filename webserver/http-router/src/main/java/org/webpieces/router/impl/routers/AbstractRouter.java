package org.webpieces.router.impl.routers;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

import com.webpieces.http2engine.api.StreamRef;

public interface AbstractRouter {

	MatchInfo getMatchInfo();

	StreamRef invoke(RequestContext ctx, ProxyStreamHandle handler);

	MatchResult2 matches(RouterRequest request, String subPath);

}
