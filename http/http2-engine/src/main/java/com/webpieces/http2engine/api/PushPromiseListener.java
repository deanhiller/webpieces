package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.lib.PartialStream;

public interface PushPromiseListener {

	/**
	 * Data comes in as a single Http2Headers, then many Http2Data if there is a payload, then a
	 * trailing SINGLE Http2Headers.  At any time, you can call response.isLastPartOfResponse
	 * to see if it is the final end of the response
	 * 
	 * @param resp
	 * @return Future that completes when we should free up more space to read in more data
	 */
	CompletableFuture<Void> incomingPushPromise(PartialStream response);
	
}
