package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.routeinvoker.ContextWrap;

public class FailureResponder {


    @Inject
    public FailureResponder(
    ) {
    }



    public CompletableFuture<Void> failureRenderingInternalServerErrorPage(RequestContext ctx, Throwable e, ResponseStreamer proxyResponse) {
        return ContextWrap.wrap(ctx, () -> proxyResponse.failureRenderingInternalServerErrorPage(e));
    }

}
