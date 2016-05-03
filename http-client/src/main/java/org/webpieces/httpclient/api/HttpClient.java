package org.webpieces.httpclient.api;

import java.net.SocketAddress;

import com.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpClient {

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
	 * @return
	 */
	public HttpSocket openHttpSocket(String idForLogging);
	
}
