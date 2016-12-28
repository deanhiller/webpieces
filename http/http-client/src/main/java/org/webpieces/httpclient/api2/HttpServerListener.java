package org.webpieces.httpclient.api2;

import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpServerListener {

	void farEndClosed(HttpSocket socket);

	/**
	 * For http/2 only in that servers can pre-emptively send a response to something not yet requested
	 * that will be requested based on the return page.  client must return an HttpSocketDataWriter that
	 * the server will stream the response data to(if any)
	 * 
	 * @param req
	 * @param resp
	 * @param isComplete
	 */
	HttpSocketDataWriter newIncomingPush(HttpRequest req, HttpResponse resp, boolean isComplete);

	void failure(Exception e);
	
}
