package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpSocket {


    /**
     *
     * Connects to an HTTP server at a given address, and returns a RequestListener
     * that one can use to send requests to that HTTP server.
     *
     * @param addr
     * @return
     */
    CompletableFuture<RequestListener> connect(InetSocketAddress addr);

	/**
	 * This can be used ONLY if 'you' know that the far end does NOT sended a chunked download.
	 * The reason is in a chunked download, we don't want to blow up your RAM.  Some apis like
	 * twitters streaming api and we would never ever be done and have a full response.  Others
	 * are just a very very large download you don't want existing in RAM anyways.
	 *
	 * @param request
	 */
	//TODO: Implement timeout for clients so that requests will timeout
	CompletableFuture<HttpResponse> send(HttpRequest request);

    RequestListener getRequestListener();

	CompletableFuture<HttpSocket> closeSocket();

	
}
