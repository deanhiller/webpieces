package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2GoAway extends Http2Frame {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.GOAWAY;
    }

    /* flags */

    /* payload */
    // 1 bit reserved
    private int lastStreamId; // 31bits
    private Http2ErrorCode errorCode; //32bits
    private DataWrapper debugData;

    public void setLastStreamId(int lastStreamId) {
        this.lastStreamId = lastStreamId & 0x7FFFFFFF; // clear the MSB for reserved
    }

    public void setErrorCode(Http2ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public int getLastStreamId() {
        return lastStreamId;
    }

    public Http2ErrorCode getErrorCode() {
        return errorCode;
    }

    public DataWrapper getDebugData() {
        return debugData;
    }

    public void setDebugData(DataWrapper debugData) {
        this.debugData = debugData;
    }

    @Override
    public String toString() {
        return "Http2GoAway{" +
                "lastStreamId=" + lastStreamId +
                ", errorCode=" + errorCode +
                ", debugData=" + debugData +
                "} " + super.toString();
    }
}
