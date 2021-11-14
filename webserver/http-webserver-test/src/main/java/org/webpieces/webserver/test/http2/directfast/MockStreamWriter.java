package org.webpieces.webserver.test.http2.directfast;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.webpieces.util.context.Context;

import java.util.HashMap;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

public class MockStreamWriter implements StreamWriter {
    private static final String IS_SERVER_SIDE = "_isServerSide";

    private StreamWriter writer;

    public MockStreamWriter(StreamWriter writer) {
        this.writer = writer;
    }

    @Override
    public XFuture<Void> processPiece(StreamMsg data) {

        Map<String, Object> context = Context.copyContext();

        //clear context for server side since client context does not leak there across socket
        Context.restoreContext(new HashMap<>());
        try {
            return writer.processPiece(data);
        } finally {
            Context.restoreContext(context);
        }
    }
}
