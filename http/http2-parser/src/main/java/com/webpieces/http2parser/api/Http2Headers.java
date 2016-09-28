package com.webpieces.http2parser.api;

import java.util.Map;

public interface Http2Headers extends Http2Frame, Http2Padded, Http2HeaderFrame {
    /* flags */
    boolean isEndStream();

    void setEndStream();

    boolean isPriority();

    void setPriority();

    /* payload */
    boolean isStreamDependencyIsExclusive();

    void setStreamDependencyIsExclusive();

    int getStreamDependency();

    void setStreamDependency(int streamDependency);

    byte getWeight();

    void setWeight(byte weight);
}
