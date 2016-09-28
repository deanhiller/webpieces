package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

public interface Http2Data extends Http2Frame, Http2Padded {
    /* flags */
    boolean isEndStream();

    void setEndStream();

    /* payload */
    DataWrapper getData();

    void setData(DataWrapper data);
}
