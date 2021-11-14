package org.webpieces.router.impl.routers;

import org.webpieces.util.futures.XFuture;
import java.util.function.Function;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class NonStreamingWebAppErrorProxy implements StreamWriter {
    private FutureHelper futureUtil;
    private final StreamWriter strWriter;
    private ProxyStreamHandle handler;
    private final Function<Throwable, XFuture<StreamWriter>> failHandler;
	private RequestContext requestCtx;

    public NonStreamingWebAppErrorProxy(
        FutureHelper futureUtil,
        StreamWriter strWriter,
        ProxyStreamHandle handler,
        RequestContext requestCtx,
        Function<Throwable, XFuture<StreamWriter>> failHandler
    ) {
        this.futureUtil = futureUtil;
        this.strWriter = strWriter;
        this.handler = handler;
		this.requestCtx = requestCtx;
        this.failHandler = failHandler;
        
    }

    @Override
    public XFuture<Void> processPiece(StreamMsg data) {
        //TODO(dhiller): Is this the proper chokepoint for streaming pieces?  Need it to be closer to
        //choke point for request headers choke point I feel like
    	Current.setContext(requestCtx);
    	try {
            return futureUtil.catchBlock(
                    () -> strWriter.processPiece(data),
                    (t) -> runRenderErrorPageIfNonStreaming(t)
            );
    	} finally {
			Current.setContext(null);
		}
    }

    private XFuture<Void> runRenderErrorPageIfNonStreaming(Throwable t) {
        //Some routes stream data in until they have all the data in which case, if anything fails, we
        //have to run the same failhandler ONLY if no response has been sent yet.....how to check on
        //the response

        if(handler.hasSentResponseAlready()) {
            return futureUtil.failedFuture(t);
        }

        return failHandler.apply(t).thenApply(s -> null);
    }

}
