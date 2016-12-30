package org.webpieces.httpclient.api;

import org.webpieces.httpclient.api.dto.Http2Headers;

public interface Http2ServerListener {

	void farEndClosed(Http2Socket socket);

	/**
	 * For http/2 only in that servers can pre-emptively send a response to something not yet requested
	 * that will be requested based on the return page.  client must return an HttpSocketDataWriter that
	 * the server will stream the response data to(if any)
	 * 
	 * @param req
	 * @param resp
	 * @param isComplete
	 */
	Http2SocketDataReader newIncomingPush(Http2Headers req, Http2Headers resp);

	void failure(Exception e);
	
}
