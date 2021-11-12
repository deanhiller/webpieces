package org.webpieces.router.impl.routers;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class NullStreamWriter implements StreamWriter {
    @Override
    public XFuture<Void> processPiece(StreamMsg data) {
        return XFuture.completedFuture(null);
    }

}
