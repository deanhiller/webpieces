package org.webpieces.router.impl.routers;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;

public interface AbstractRouter {

	MatchInfo getMatchInfo();

	RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler);

	MatchResult2 matches(RouterRequest request, String subPath);

}
