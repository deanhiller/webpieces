package com.webpieces.http2parser.api.highlevel;

public interface Http2Payload {

	int getStreamId();
	
	boolean isEndStream();
}
