package com.webpieces.http2engine.api;

public interface Http2Payload {

	int getStreamId();
	
	boolean isEndStream();
}
