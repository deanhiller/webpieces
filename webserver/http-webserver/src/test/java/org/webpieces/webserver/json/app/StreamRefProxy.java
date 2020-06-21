package org.webpieces.webserver.json.app;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class StreamRefProxy implements StreamRef {
    private static final Logger log = LoggerFactory.getLogger(StreamRefProxy.class);

    private final CompletableFuture<StreamWriter> writer;
    private final CompletableFuture<StreamRef> futureStream;

    public StreamRefProxy(CompletableFuture<StreamWriter> writer, CompletableFuture<StreamRef> futureStream) {
        this.writer = writer;
        this.futureStream = futureStream;
    }

    @Override
    public CompletableFuture<StreamWriter> getWriter() {
        return writer;
    }

    @Override
    public CompletableFuture<Void> cancel(CancelReason reason) {
        //we can't block here or async sequential chain here since a futureStream may never come into
        //existence like if authentication fails.  ie. futureStream MAY NEVER resolve.
        futureStream
                .thenCompose( streamRef -> streamRef.cancel(reason))
                .exceptionally( t -> {
                    log.error("Could not cancel existing stream", t);
                    return null;
                });

        return CompletableFuture.completedFuture(null);
    }
}