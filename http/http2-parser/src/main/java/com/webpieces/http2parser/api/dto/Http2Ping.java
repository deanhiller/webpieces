package com.webpieces.http2parser.api.dto;

public class Http2Ping extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.PING;
	}

	/* flags */
	private boolean isPingResponse; /* 0x1 */

	/* payload */
	private Long opaqueData;
}
