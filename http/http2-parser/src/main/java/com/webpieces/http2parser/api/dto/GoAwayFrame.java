package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;

public class GoAwayFrame extends AbstractHttp2Frame implements Http2Msg {

    /* flags */

    /* payload */
    // 1 bit reserved
    private long lastStreamId; // 31bits
    private long errorCode; //32bits
    private DataWrapper debugData;

    public void setLastStreamId(long lastStreamId) {
        this.lastStreamId = lastStreamId;
    }
    public long getLastStreamId() {
        return lastStreamId;
    }

    public long getErrorCode() {
    	return errorCode;
    }

    public void setErrorCode(long code) {
    	this.errorCode = code;
    }
    
    public void setKnownErrorCode(Http2ErrorCode errorCode) {
        this.errorCode = errorCode.getCode();
    }
    
    public Http2ErrorCode getKnownErrorCode() {
    	return Http2ErrorCode.translate(errorCode);
    }

    public DataWrapper getDebugData() {
        return debugData;
    }
    public void setDebugData(DataWrapper debugData) {
        this.debugData = debugData;
    }

    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.GOAWAY;
    }
    @Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.GOAWAY;
	}
    
    @Override
    public String toString() {
        return "GoAwayFrame{" +
        		super.toString() +
                ", lastStreamId=" + lastStreamId +
                ", errorCode=" + errorCode +
                ", debugData.len=" + debugData.getReadableSize() +
                "}";
    }
}
