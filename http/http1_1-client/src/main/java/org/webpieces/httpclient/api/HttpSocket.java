package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpSocket {

	public CompletableFuture<HttpSocket> connect(InetSocketAddress addr);

	/**
	 * This can be used ONLY if 'you' know that the far end does NOT sended a chunked download. 
	 * The reason is in a chunked download, we don't want to blow up your RAM.  Some apis like
	 * twitters streaming api and we would never ever be done and have a full response.  Others
	 * are just a very very large download you don't want existing in RAM anyways.
	 * 
	 * @param request
	 */
	//TODO: Implement timeout for clients so that requests will timeout
	public CompletableFuture<HttpFullResponse> send(HttpFullRequest request);

	public CompletableFuture<HttpDataWriter> send(HttpRequest request, HttpResponseListener l);

	public CompletableFuture<HttpSocket> close();

}
