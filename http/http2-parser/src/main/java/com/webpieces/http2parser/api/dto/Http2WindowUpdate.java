package com.webpieces.http2parser.api.dto;

public class Http2WindowUpdate extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.WINDOW_UPDATE;
	}
	/* flags */

	/* payload */
	//1bit reserved
	private int windowSizeIncrement; //31 bits
	
}
