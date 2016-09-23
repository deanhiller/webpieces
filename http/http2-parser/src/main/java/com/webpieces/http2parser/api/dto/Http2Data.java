package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2Data extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.DATA;
	}

	/* flags */
	private boolean endStream; /* 0x1 */
	private boolean padded;    /* 0x8 */

	/* payload */
	private DataWrapper data;
}
