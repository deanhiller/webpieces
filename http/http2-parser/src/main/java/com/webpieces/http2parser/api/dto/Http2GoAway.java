package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2GoAway extends AbstractHttp2Frame {
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

    @Override
	public void setStreamId(int streamId) {
    	if(streamId == 0)
    		return; //nothing to do as we are fixed at 0
    	throw new UnsupportedOperationException("Http2Settings can never be any other stream id except 0 which is already set");
	}
	@Override
	public int getStreamId() {
		return 0;
	}
	
    public void setLastStreamId(int lastStreamId) {
        this.lastStreamId = lastStreamId & 0x7FFFFFFF; // clear the MSB for reserved
    }
    public int getLastStreamId() {
        return lastStreamId;
    }

    public void setErrorCode(Http2ErrorCode errorCode) {
        this.errorCode = errorCode;
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
        		super.toString() +
                ", lastStreamId=" + lastStreamId +
                ", errorCode=" + errorCode +
                ", debugData.len=" + debugData.getReadableSize() +
                "}";
    }
}
