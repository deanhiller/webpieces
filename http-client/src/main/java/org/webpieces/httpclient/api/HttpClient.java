package org.webpieces.httpclient.api;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpClient {

	/**
	 * 
	 */
	public CompletableFuture<HttpResponse> sendSingleRequest(SocketAddress addr, HttpRequest req);
	
	/**
	 * Opens and closes a single socket connection to send a request and receive a
	 * response (therefore it is not very efficient, so you could use openHttpSocket instead)
	 * 
	 * @param request
	 * @param cb
	 */
	public void sendSingleRequest(SocketAddress addr, HttpRequest request, ResponseListener l);
	
	/**
	 * HttpSocket is so you can send multiple requests or if you expect server to stream
	 * back to you or expect a chunked download
	 * 
	 * @param listener - some clients need to know if far end closed the socket after pipelining
	 * request after request
	 * 
	 * @return
	 */
	public HttpSocket openHttpSocket(String idForLogging, CloseListener listener);
	
}
