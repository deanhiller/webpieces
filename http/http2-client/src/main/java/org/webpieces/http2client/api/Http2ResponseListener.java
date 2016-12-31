package org.webpieces.http2client.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.dto.Http2Headers;

import com.webpieces.http2parser.api.dto.Http2UnknownFrame;

public interface Http2ResponseListener {

	/**
	 * 
	 * @param req The request the client originally sent OR in the case of a PUSH_PROMISE, the request the
	 * server is pre-emptively sending back before you call anything
	 * 
	 * @param resp
	 * @param isComplete
	 */
	void incomingResponse(Http2Headers resp, boolean isComplete);

	void incomingData(DataWrapper data, boolean isComplete);
	
	void incomingEndHeaders(Http2Headers headers, boolean isComplete);
	
	void incomingUnknownFrame(Http2UnknownFrame frame, boolean isComplete);
	
	void serverCancelledRequest();
	
}
