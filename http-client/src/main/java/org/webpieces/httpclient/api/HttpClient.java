package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpClient {

	/*
	 * This can be used ONLY if 'you' know that the far end does NOT sended a chunked download. 
	 * The reason is in a chunked download, we don't want to blow up your RAM.  Some apis like
	 * twitters streaming api and we would never ever be done and have a full response.  Others
	 * are just a very very large download you don't want existing in RAM anyways.
	 * 
	 * @param request
	 */
	public CompletableFuture<HttpResponse> sendSingleRequest(InetSocketAddress addr, HttpRequest request);
	
	/**
	 * Opens and closes a single socket connection to send a request and receive a
	 * response (therefore it is not very efficient, so you could use openHttpSocket instead).
	 * ResponseListener will be called for the initial response and then called for every chunk
	 * of the download after that.
	 * 
	 * @param request
	 * @param cb
	 */
	public void sendSingleRequest(InetSocketAddress addr, HttpRequest request, ResponseListener l);

	/**
	 * HttpSocket is so you can send multiple requests
	 * 
	 * @return
	 */
	public HttpSocket openHttpSocket(String idForLogging);
	
	/**
	 * Feel free to use openHttpSocket(String idForLogging) unless you want to be notified of the 
	 * far end server closing it's connection.  Typically you will not need this, but if you 
	 * really want notification of the far end closing it's socket, this is how.  
	 * 
	 * If you have any outstanding responses and the connection closes, your ResponseListener
	 * is notified anyways or the CompletableFuture is resolved exceptionally with the 
	 * channel closed before a response.  This is mostly so you are notified after all responses.
	 * 
	 * @return
	 */
	public HttpSocket openHttpSocket(String idForLogging, CloseListener listener);
	
}
