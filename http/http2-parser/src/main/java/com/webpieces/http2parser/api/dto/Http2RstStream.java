package com.webpieces.http2parser.api.dto;

public class Http2RstStream {
	public Http2FrameType getFrameType() {
		return Http2FrameType.RST_STREAM;
	}
	/* flags */

	/* payload */
	private long errorCode; //32 bits
	
}
