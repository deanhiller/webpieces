package com.webpieces.http2parser.api.dto;

public class Http2Settings {
	/* Default settings */
	public Http2Settings() {
		headerTableSize = 4096L;
		enablePush = true;
		maxConcurrentStreams = -1L; /* Unlimited */
		initialWindowSize = 65535L;
		maxFrameSize = 16384L;
		maxHeaderListSize = -1L;
	}

	private Long headerTableSize;
	private Boolean enablePush;
	private Long maxConcurrentStreams;
	private Long initialWindowSize;
	private Long maxFrameSize;
	private Long maxHeaderListSize;

	
}
