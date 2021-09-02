package org.webpieces.webserver.test.http2.directfast;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

import java.util.concurrent.CompletableFuture;

public class MockProxyStreamRef implements StreamRef {
    private StreamRef streamRef;

    public MockProxyStreamRef(StreamRef streamRef) {

        this.streamRef = streamRef;
    }

    @Override
    public CompletableFuture<StreamWriter> getWriter() {
        return streamRef.getWriter().thenApply(writer -> new MockStreamWriter(writer));
    }

    @Override
    public CompletableFuture<Void> cancel(CancelReason reason) {
        return streamRef.cancel(reason);
    }
}
