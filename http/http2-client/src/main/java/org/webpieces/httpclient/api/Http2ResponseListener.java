package org.webpieces.httpclient.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.dto.Http2Headers;

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
	void incomingResponse(Http2Headers resp);

	void incomingData(DataWrapper data);
	
	void incomingEndHeaders(Http2Headers headers);
	
	void incomingUnknownFrame(Http2UnknownFrame frame);
	
	void serverCancelledRequest();
	
}
