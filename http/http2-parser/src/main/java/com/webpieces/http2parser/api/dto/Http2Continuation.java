package com.webpieces.http2parser.api.dto;

public class Http2Continuation extends Http2Frame {

	public Http2FrameType getFrameType() {
		return Http2FrameType.CONTINUATION;
	}

	/* Flags */
	private boolean endHeaders; /* 0x4 */

	/* payload */
	private Http2HeaderBlock headerBlock;

}
