package org.webpieces.router.impl.routeinvoker;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

import java.util.concurrent.CompletableFuture;

public class NullWriter implements StreamWriter {
    @Override
    public CompletableFuture<Void> processPiece(StreamMsg data) {
        return CompletableFuture.completedFuture(null);
    }
}
