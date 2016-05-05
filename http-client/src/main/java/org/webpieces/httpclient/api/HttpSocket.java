package org.webpieces.httpclient.api;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpSocket {

	public CompletableFuture<HttpSocket> connect(SocketAddress addr);

	/**
	 * This can be used ONLY if 'you' know that the far end does NOT sended a chunked download. 
	 * The reason is in a chunked download, we don't want to blow up your RAM.  Some apis like
	 * twitters streaming api and we would never ever be done and have a full response.  Others
	 * are just a very very large download you don't want existing in RAM anyways.
	 * 
	 * @param request
	 * @param cb
	 */
	public CompletableFuture<HttpResponse> send(HttpRequest request);
	
	public void send(HttpRequest request, ResponseListener l);

	public CompletableFuture<HttpSocket> closeSocket();
	
}
