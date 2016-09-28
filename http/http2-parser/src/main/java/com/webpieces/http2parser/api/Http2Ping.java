package com.webpieces.http2parser.api;

public interface Http2Ping extends Http2Frame {
    /* flags */
    boolean isPingResponse();

    void setPingResponse();

    /* payload */
    long getOpaqueData();

    void setOpaqueData(long opaqueData);
}
