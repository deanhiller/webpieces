package com.webpieces.http2engine.api.dto;

public interface PartialStream {

	boolean isEndOfStream();
	
	int getStreamId();
}
