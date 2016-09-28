package com.webpieces.http2parser.api;

import com.webpieces.http2parser.impl.Http2ErrorCode;

public interface Http2RstStream extends Http2Frame {
    Http2ErrorCode getErrorCode();
    void setErrorCode(Http2ErrorCode errorCode);
}
