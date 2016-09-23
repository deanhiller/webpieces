package com.webpieces.http2parser.api.dto;

public class Http2PushPromise {

	private long promisedStreamId;
	private Http2HeaderBlock headerBlock;
	private boolean endHeaders;
	
}
