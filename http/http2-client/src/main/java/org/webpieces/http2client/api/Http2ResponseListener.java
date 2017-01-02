package org.webpieces.http2client.api;

import com.webpieces.http2engine.api.dto.PartialStream;

public interface Http2ResponseListener {

	/**
	 * Data comes in as a single Http2Headers, then many Http2Data if there is a payload, then a
	 * trailing SINGLE Http2Headers.  At any time, you can call response.isLastPartOfResponse
	 * to see if it is the final end of the response
	 * 
	 * @param resp
	 */
	void incomingPartialResponse(PartialStream response);

	/**
	 * For http/2 only in that servers can pre-emptively send a response to requests
	 * that are about to happen based on the first request.  The Http/2 Engine will call this
	 * method and then invoke the methods in your instance multiple times.  It is best you return
	 * a new implementation each time as each push is separate and can be intermingled as well.
	 * 
	 * @param req
	 * @param resp
	 * @param isComplete
	 */
	PushPromiseListener newIncomingPush(int streamId);
	
	void serverCancelledRequest();
	
}
