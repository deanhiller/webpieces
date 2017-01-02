package com.webpieces.http2engine.api;

import com.webpieces.http2engine.api.dto.PartialStream;

public interface PushPromiseListener {

	/**
	 * Data comes in as a single Http2Headers, then many Http2Data if there is a payload, then a
	 * trailing SINGLE Http2Headers.  At any time, you can call response.isLastPartOfResponse
	 * to see if it is the final end of the response
	 * 
	 * @param resp
	 */
	void incomingPushPromise(PartialStream response);
	
}
