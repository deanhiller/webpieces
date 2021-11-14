package org.webpieces.webserver.json.app;

import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class StreamRefProxy implements StreamRef {
    private static final Logger log = LoggerFactory.getLogger(StreamRefProxy.class);

    private final XFuture<StreamWriter> writer;
    private final XFuture<StreamRef> futureStream;

    public StreamRefProxy(XFuture<StreamWriter> writer, XFuture<StreamRef> futureStream) {
        this.writer = writer;
        this.futureStream = futureStream;
    }

    @Override
    public XFuture<StreamWriter> getWriter() {
        return writer;
    }

    @Override
    public XFuture<Void> cancel(CancelReason reason) {
        //we can't block here or async sequential chain here since a futureStream may never come into
        //existence like if authentication fails.  ie. futureStream MAY NEVER resolve.
        futureStream
                .thenCompose( streamRef -> streamRef.cancel(reason))
                .exceptionally( t -> {
                    log.error("Could not cancel existing stream", t);
                    return null;
                });

        return XFuture.completedFuture(null);
    }
}