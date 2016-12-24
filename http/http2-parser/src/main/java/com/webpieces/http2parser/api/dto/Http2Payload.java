package com.webpieces.http2parser.api.dto;

public class Http2Payload {

	private boolean isUnknownFrameType;
	private Http2Frame frame;
	
	public Http2Payload(boolean isUnknownFrameType, Http2Frame frame) {
		this.isUnknownFrameType = isUnknownFrameType;
		this.frame = frame;
	}

	public boolean isUnknownFrameType() {
		return isUnknownFrameType;
	}

	public Http2Frame getFrame() {
		return frame;
	}
	
}
