package com.webpieces.http2parser.api;

public interface Http2PushPromise extends Http2Frame, Http2Padded, Http2HeaderFrame {
    /* flags */

    /* payload */
    int getPromisedStreamId();

    void setPromisedStreamId(int promisedStreamId);
}
