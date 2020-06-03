package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class NullStreamWriter implements StreamWriter {
    @Override
    public CompletableFuture<Void> processPiece(StreamMsg data) {
        return CompletableFuture.completedFuture(null);
    }

}
