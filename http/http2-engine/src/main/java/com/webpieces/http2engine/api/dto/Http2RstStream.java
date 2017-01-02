package com.webpieces.http2engine.api.dto;

import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class Http2RstStream implements PartialStream {
	private int streamId;
    private Http2ErrorCode errorCode;
    
	public int getStreamId() {
		return streamId;
	}
	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	public Http2ErrorCode getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(Http2ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	@Override
	public String toString() {
		return "Http2RstStream [streamId=" + streamId + ", errorCode=" + errorCode + "]";
	}
	@Override
	public boolean isEndOfStream() {
		return true;
	}
    
}
