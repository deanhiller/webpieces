package org.webpieces.httpclient.api;

import org.webpieces.util.futures.Future;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpClient {

	/**
	 * Opens and closes a single socket connection to send a request and receive a
	 * response (therefore it is not very efficient)
	 * 
	 * @param request
	 * @param cb
	 */
	public Future<HttpResponse, Throwable> sendSingleRequest(HttpRequest request);
	
	

}
