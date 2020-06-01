package com.webpieces.http2engine.api;

import com.webpieces.hpack.api.dto.Http2Response;

public interface ResponseStreamHandle {

	/**
	 * For Http2ClientEngine, this receives the Http2 Response and for Http2ServerEngine, you call this method
	 * to send a response
	 */
	StreamRef process(Http2Response response);

	/**
	 * This does nothing in http1.1. 
	 * 
	 * For Http2ClientEngine, this receives the Http2 Push and for Http2ServerEngine, you call this method
	 * to send a push
	 */
	PushStreamHandle openPushStream();
	
}