package com.webpieces.http2engine.api;

public interface PartialStream {

	boolean isEndOfStream();
	
	int getStreamId();
}
