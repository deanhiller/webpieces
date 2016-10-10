package com.webpieces.http2parser.api.dto;

public class Http2RstStream extends Http2Frame {
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
        return "Http2RstStream{" +
                "errorCode=" + errorCode +
                "} " + super.toString();
    }
}
