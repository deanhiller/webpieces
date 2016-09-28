package com.webpieces.http2parser.api;

import java.util.Map;

public interface Http2PushPromise extends Http2Frame, Http2Padded, Http2HeaderFrame {
    /* flags */

    /* payload */
    int getPromisedStreamId();

    void setPromisedStreamId(int promisedStreamId);
}
