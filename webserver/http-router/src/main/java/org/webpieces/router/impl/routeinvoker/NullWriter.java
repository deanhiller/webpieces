package org.webpieces.router.impl.routeinvoker;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;

public class NullWriter implements StreamWriter {
    @Override
    public XFuture<Void> processPiece(StreamMsg data) {
        return XFuture.completedFuture(null);
    }
}
