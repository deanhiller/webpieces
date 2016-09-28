package com.webpieces.http2parser.api;

public interface Http2WindowUpdate extends Http2Frame {
    int getWindowSizeIncrement();

    void setWindowSizeIncrement(int windowSizeIncrement);
}
