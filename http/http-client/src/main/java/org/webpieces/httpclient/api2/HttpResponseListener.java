package org.webpieces.httpclient.api2;

import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpResponseListener {

	/**
	 * 
	 * @param req The request the client originally sent OR in the case of a PUSH_PROMISE, the request the
	 * server is pre-emptively sending back before you call anything
	 * 
	 * @param resp
	 * @param isComplete
	 */
	void incomingResponse(HttpResponse resp, boolean isComplete);

	void incomingData(HttpChunk chunk, boolean isComplete);
	
	void serverCancelledRequest();
	
}
