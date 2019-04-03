package org.webpieces.router.impl.routing;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.model.MatchResult2;

public class ContentRouter implements AbstractRouter {

	@Override
	public String getFullPath() {
		return null;
	}

	@Override
	public Port getExposedPorts() {
		return null;
	}

	@Override
	public MatchResult2 matches2(RouterRequest request, String subPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb,
			Map<String, String> pathParams) {
		// TODO Auto-generated method stub
		return null;
	}

}
