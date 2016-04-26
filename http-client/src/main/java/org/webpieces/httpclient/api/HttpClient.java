package org.webpieces.httpclient.api;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

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
	public CompletableFuture<HttpResponse> sendSingleRequest(HttpRequest request);
	
	/**
	 * HttpSocket is so you can send multiple requests or if you expect server to stream
	 * back to you or expect a chunked download
	 * 
	 * @return
	 */
	public HttpSocket openHttpSocket(SocketAddress addr);
	
}
