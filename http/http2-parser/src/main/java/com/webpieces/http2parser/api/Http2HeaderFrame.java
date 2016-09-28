package com.webpieces.http2parser.api;

import java.util.Map;

public interface Http2HeaderFrame {
    /* flags */
    boolean isEndHeaders();

    void setEndHeaders();

    /* payload */
    Map<String, String> getHeaders();

    void setHeaders(Map<String, String> headers);
}
