package com.webpieces.http2parser.api;

public interface Http2Priority extends Http2Frame {
    /* flags */

    /* payload */
    boolean isStreamDependencyIsExclusive();

    void setStreamDependencyIsExclusive();

    int getStreamDependency();

    void setStreamDependency(int streamDependency);

    byte getWeight();

    void setWeight(byte weight);
}
