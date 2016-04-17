package org.webpieces.httpclient.api;

import com.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpClient {

	/**
	 * Opens and closes a single socket connection to send a request and receive a
	 * response (therefore it is not very efficient)
	 * 
	 * @param request
	 * @param cb
	 */
	public void sendSingleRequest(HttpRequest request, HttpCallback cb);
	

}
