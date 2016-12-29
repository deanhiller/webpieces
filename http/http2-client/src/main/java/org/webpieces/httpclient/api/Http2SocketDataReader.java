package org.webpieces.httpclient.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.dto.Http2EndHeaders;

public interface Http2SocketDataReader {

	/**
	 * Send HttpLastChunk to end the streaming of the chunks
	 * @param resp
	 */
	void incomingData(DataWrapper data);

	/**
	 * Sent after ALL data has been sent and may be empty if server sent none
	 * @param endHeaders
	 */
	void incomingTrailingHeaders(Http2EndHeaders endHeaders);

	/**
	 * In http/2, sends a stream reset to cancel the request.  In http1.1, throws an exception
	 * since requests can't be cancelled
	 */
	void serverCancelledRequest();
	
}
