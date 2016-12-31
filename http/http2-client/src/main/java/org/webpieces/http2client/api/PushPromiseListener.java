package org.webpieces.http2client.api;

import org.webpieces.http2client.api.dto.PartialResponse;

public interface PushPromiseListener {

	/**
	 * Data comes in as a single Http2Headers, then many Http2Data if there is a payload, then a
	 * trailing SINGLE Http2Headers.  At any time, you can call response.isLastPartOfResponse
	 * to see if it is the final end of the response
	 * 
	 * @param resp
	 */
	void incomingPushPromise(PartialResponse response);
	
	/**
	 * In http/2, sends a stream reset to cancel the request.  In http1.1, throws an exception
	 * since requests can't be cancelled
	 */
	void serverCancelledRequest();
	
}
