package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.proxyout.ChannelCloser;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.proxyout.ResponseCreator;
import org.webpieces.router.impl.routeinvoker.ContextWrap;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;

public class FailureResponder {

    private static final Logger log = LoggerFactory.getLogger(FailureResponder.class);
    private ChannelCloser channelCloser;
    private ResponseCreator responseCreator;

    @Inject
    public FailureResponder(
        ChannelCloser channelCloser,
        ResponseCreator responseCreator
    ) {
        this.channelCloser = channelCloser;
        this.responseCreator = responseCreator;
    }

    public CompletableFuture<StreamWriter> sendRedirectAndClearCookie(ProxyStreamHandle stream, RouterRequest req, String badCookieName) {
        RedirectResponse httpResponse = new RedirectResponse(false, req.isHttps, req.domain, req.port, req.relativePath);
        Http2Response response = responseCreator.createRedirect(req.orginalRequest, httpResponse);

        responseCreator.addDeleteCookie(response, badCookieName);

        log.info("sending REDIRECT(due to bad cookie) response responseSender="+ stream);
        CompletableFuture<StreamWriter> future = stream.process(response);

        channelCloser.closeIfNeeded(req.orginalRequest, stream);

        return future.thenApply(s -> null);
    }

    public CompletableFuture<Void> failureRenderingInternalServerErrorPage(RequestContext ctx, Throwable e, ResponseStreamer proxyResponse) {
        return ContextWrap.wrap(ctx, () -> proxyResponse.failureRenderingInternalServerErrorPage(e));
    }

}
