package org.webpieces.router.impl.routers;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class EmptyWriter implements StreamWriter {
    private static final Logger log = LoggerFactory.getLogger(EmptyWriter.class);
    @Override
    public CompletableFuture<Void> processPiece(StreamMsg data) {
        log.warn("OPTION request spec says no body but someone is sending a body so we are discarding it");
        return CompletableFuture.completedFuture(null);
    }
}
