package com.webpieces.http2parser.api.dto;

public class Http2Headers extends Http2Frame {
	public Http2FrameType getFrameType() {
		return Http2FrameType.HEADERS;
	}

	/* flags */
	private boolean endStream; /* 0x1 */
	private boolean endHeaders; /* 0x4 */
	private boolean padded; /* 0x8 */
	private boolean priority; /* 0x20 */

	/* payload */
	private boolean streamDependencyIsExclusive; //1 bit
	private int streamDependency; //31 bits
	private short weight; //8 bits
	private Http2HeaderBlock headerBlock;
	
	
}
