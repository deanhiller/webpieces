package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class NonStreamingWebAppErrorProxy implements StreamWriter {
    private FutureHelper futureUtil;
    private final StreamWriter strWriter;
    private ProxyStreamHandle handler;
    private final Function<Throwable, RouterStreamRef> failHandler;

    public NonStreamingWebAppErrorProxy(
        FutureHelper futureUtil,
        StreamWriter strWriter,
        ProxyStreamHandle handler,
        Function<Throwable, RouterStreamRef> failHandler
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

        RouterStreamRef ref = failHandler.apply(t);
        return ref.getWriter().thenApply(s -> null);
    }

}
