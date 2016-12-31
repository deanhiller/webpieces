package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class RstStreamFrame extends AbstractHttp2Frame {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.RST_STREAM;
    }

    /* flags */

    /* payload */
    private Http2ErrorCode errorCode; //32 bits

    public Http2ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Http2ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "RstStreamFrame{" +
        		"streamId=" + super.toString() +
                "errorCode=" + errorCode +
                "} ";
    }
}
