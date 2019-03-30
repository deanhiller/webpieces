package org.webpieces.router.impl.ctx;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;

public class ResponseFailureProcessor extends Processor {

	private ResponseStreamer responseCb;

	public ResponseFailureProcessor(RequestContext ctx, ResponseStreamer responseCb) {
		super(ctx);
		this.responseCb = responseCb;
	}

	public CompletableFuture<Void> failureRenderingInternalServerErrorPage(Throwable e) {
		return wrapFunctionInContext(() -> responseCb.failureRenderingInternalServerErrorPage(e));
	}
}
