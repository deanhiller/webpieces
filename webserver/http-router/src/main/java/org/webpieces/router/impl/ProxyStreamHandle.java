package org.webpieces.router.impl;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;
import org.slf4j.MDC;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.util.futures.FutureHelper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProxyStreamHandle implements RouterStreamHandle {
    private String txId;
    private RouterStreamHandle handler;
    private FutureHelper futureUtil;
    private Http2Response lastResponseSent;

    public ProxyStreamHandle(String txId, RouterStreamHandle handler, FutureHelper futureUtil) {
        this.txId = txId;
        this.handler = handler;
        this.futureUtil = futureUtil;
    }

    @Override
    public CompletableFuture<StreamWriter> process(Http2Response response) {
        if(lastResponseSent != null)
            throw new IllegalStateException("You already sent a response.  "
                    + "do not call Actions.redirect or Actions.render more than once.  previous response="
                    + lastResponseSent +" 2nd response="+response);
        lastResponseSent = response;

        MDC.put("txId", txId);
        return handler.process(response)
                .thenApply(w -> new ProxyStreamWriter(txId, w));
    }

    public boolean hasSentResponseAlready() {
        return lastResponseSent != null;
    }

    private static class ProxyStreamWriter implements StreamWriter {

        private final String txId;
        private final StreamWriter w;

        public ProxyStreamWriter(String txId, StreamWriter w) {
            this.txId = txId;
            this.w = w;
        }

        @Override
        public CompletableFuture<Void> processPiece(StreamMsg data) {
            MDC.put("txId", txId);
            return w.processPiece(data);
        }
    }

    @Override
    public Object getSocket() {
        return handler.getSocket();
    }

    @Override
    public Map<String, Object> getSession() {
        return handler.getSession();
    }

    @Override
    public boolean requestCameFromHttpsSocket() {
        return handler.requestCameFromHttpsSocket();
    }

    @Override
    public boolean requestCameFromBackendSocket() {
        return handler.requestCameFromBackendSocket();
    }

    @Deprecated
    @Override
    public Void closeIfNeeded() {
        return handler.closeIfNeeded();
    }

    @Override
    public PushStreamHandle openPushStream() {
        return handler.openPushStream();
    }

    @Override
    public CompletableFuture<Void> cancel(CancelReason payload) {
        return handler.cancel(payload);
    }
}
