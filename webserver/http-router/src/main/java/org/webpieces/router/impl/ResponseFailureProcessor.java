package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.routeinvoker.ContextWrap;

public class ResponseFailureProcessor {
	
	private final RequestContext ctx;
	private final ResponseStreamer responseCb;

	public ResponseFailureProcessor(RequestContext ctx, ResponseStreamer responseCb) {
		this.ctx = ctx;
		this.responseCb = responseCb;
	}

	public CompletableFuture<Void> failureRenderingInternalServerErrorPage(Throwable e) {
		return ContextWrap.wrap(ctx, () -> responseCb.failureRenderingInternalServerErrorPage(e));
	}
}
