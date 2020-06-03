package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class NullWriter implements StreamWriter {
    @Override
    public CompletableFuture<Void> processPiece(StreamMsg data) {
        return CompletableFuture.completedFuture(null);
    }
}
