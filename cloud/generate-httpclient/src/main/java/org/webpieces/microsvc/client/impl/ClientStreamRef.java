package org.webpieces.microsvc.client.impl;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ClientStreamRef implements StreamRef {

    private static final Logger log = LoggerFactory.getLogger(ClientStreamRef.class);

    private final CompletableFuture<StreamWriter> writer;
    private final CompletableFuture<StreamRef> aFutureStreamRef;

    public ClientStreamRef(CompletableFuture<StreamWriter> writer, CompletableFuture<StreamRef> aFutureStreamRef) {
        this.writer = writer;
        this.aFutureStreamRef = aFutureStreamRef;
    }

    @Override
    public CompletableFuture<StreamWriter> getWriter() {
        return writer;
    }

    @Override
    public CompletableFuture<Void> cancel(CancelReason reason) {

        log.info("Client receiving cancel to shutdown stream");

        //Since a stream ref MAY or MAY NOT ever come into existence, we cannot chain here
        //for completion but can only async cancel it
        aFutureStreamRef
            .thenCompose(streamRef -> streamRef.cancel(reason))
            .exceptionally(t -> {
                log.error("Exception cancelling stream client side", t);
                return null;
            });

        //The cance is async here on purpose as we can't block on something that may never happen
        return CompletableFuture.completedFuture(null);

    }

}
