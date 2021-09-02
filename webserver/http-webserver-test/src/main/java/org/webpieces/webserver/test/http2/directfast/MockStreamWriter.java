package org.webpieces.webserver.test.http2.directfast;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.webpieces.util.context.Context;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockStreamWriter implements StreamWriter {
    private static final String IS_SERVER_SIDE = "_isServerSide";

    private StreamWriter writer;

    public MockStreamWriter(StreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public CompletableFuture<Void> processPiece(StreamMsg data) {

        Boolean isServerSide = (Boolean) Context.get(IS_SERVER_SIDE);

        Map<String, Object> context = Context.copyContext();
        Context.set(IS_SERVER_SIDE, Boolean.TRUE);
        try {
            return writer.processPiece(data);
        } finally {
            if(isServerSide == null) {
                //We must simulate being separate from the webserver and the webserver sets and
                //clears the context so we need to capture context and restore it here for tests
                //since everything is single threaded, the server loops around in which case, we
                //do not want to touch the server's context
                Context.restoreContext(context);
            }
        }
    }
}
