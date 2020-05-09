package org.webpieces.router.impl.routers;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.impl.ProxyStreamHandle;
import org.webpieces.util.futures.FutureHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class NonStreamingWebAppErrorProxy implements StreamWriter {
    private FutureHelper futureUtil;
    private final StreamWriter strWriter;
    private ProxyStreamHandle handler;
    private final Function<Throwable, CompletableFuture<StreamWriter>> failHandler;

    public NonStreamingWebAppErrorProxy(
        FutureHelper futureUtil,
        StreamWriter strWriter,
        ProxyStreamHandle handler,
        Function<Throwable, CompletableFuture<StreamWriter>> failHandler
    ) {
        this.futureUtil = futureUtil;
        this.strWriter = strWriter;
        this.handler = handler;
        this.failHandler = failHandler;
    }

    @Override
    public CompletableFuture<Void> processPiece(StreamMsg data) {
        return futureUtil.catchBlock(
                () -> strWriter.processPiece(data),
                (t) -> runRenderErrorPageIfNonStreaming(t)
        );
    }

    private CompletableFuture<Void> runRenderErrorPageIfNonStreaming(Throwable t) {
        //Some routes stream data in until they have all the data in which case, if anything fails, we
        //have to run the same failhandler ONLY if no response has been sent yet.....how to check on
        //the response

        if(handler.hasSentResponseAlready()) {
            return futureUtil.failedFuture(t);
        }

        return failHandler.apply(t).thenApply(s -> null);
    }

}
