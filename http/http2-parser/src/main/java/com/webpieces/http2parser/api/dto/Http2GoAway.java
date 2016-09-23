package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2GoAway extends Http2Frame {

	public Http2FrameType getFrameType() {
		return Http2FrameType.GOAWAY;
	}

	/* flags */

	/* payload */
	// 1 bit reserved
	private int lastStreamId; // 31bits
	private long errorCode; //32bits

	private DataWrapper debugData;
}
