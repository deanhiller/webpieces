package com.webpieces.http2parser.api.dto;

public class Http2PushPromise extends Http2Frame {

	public Http2FrameType getFrameType() {
		return Http2FrameType.PUSH_PROMISE;
	}

	/* flags */
	private boolean endHeaders; /* 0x4 */
	private boolean padded; /* 0x8 */

	// reserved - 1bit
	private long promisedStreamId; //31bits
	private Http2HeaderBlock headerBlock;
}
