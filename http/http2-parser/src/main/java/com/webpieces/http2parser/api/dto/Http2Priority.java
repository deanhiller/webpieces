package com.webpieces.http2parser.api.dto;

public class Http2Priority extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.PRIORITY;
	}

	/* flags */

	/* payload */
	private boolean streamDependencyIsExclusive; //1 bit
	private int streamDependency; //31 bits
	private short weight; //8 bits
	
	
}
