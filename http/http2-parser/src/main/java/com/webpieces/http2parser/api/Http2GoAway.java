package com.webpieces.http2parser.api;

import com.webpieces.http2parser.impl.Http2ErrorCode;
import org.webpieces.data.api.DataWrapper;

public interface Http2GoAway extends Http2Frame {
    int getLastStreamId();

    void setLastStreamId(int lastStreamId);

    Http2ErrorCode getErrorCode();

    void setErrorCode(Http2ErrorCode errorCode);

    DataWrapper getDebugData();

    void setDebugData(DataWrapper debugData);
}
