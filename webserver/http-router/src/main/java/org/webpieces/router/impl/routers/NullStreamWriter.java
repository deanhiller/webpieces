package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class NullStreamWriter implements StreamWriter {
    @Override
    public CompletableFuture<Void> processPiece(StreamMsg data) {
        return CompletableFuture.completedFuture(null);
    }

}
