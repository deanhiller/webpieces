package com.webpieces.http2parser.api.dto;

public class Http2Settings extends Http2Frame {
	/* Default settings */
	public Http2Settings() {
		headerTableSize = 4096L;
		enablePush = true;
		maxConcurrentStreams = -1L; /* Unlimited */
		initialWindowSize = 65535L;
		maxFrameSize = 16384L;
		maxHeaderListSize = -1L;
	}

	public Http2FrameType getFrameType() {
        return Http2FrameType.SETTINGS;
    }

	/* flags */
	private boolean ack; /* 0x1 */

    /* payload */
    // 16bits identifier
    // 32bits value
	private Long headerTableSize; /* 0x1 */
	private Boolean enablePush;   /* 0x2 */
	private Long maxConcurrentStreams; /* 0x3 */
	private Long initialWindowSize; /* 0x4 */
	private Long maxFrameSize; /* 0x5 */
	private Long maxHeaderListSize; /* 0x6 */

	
}
