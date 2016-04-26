package org.webpieces.httpclient.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpSocket {

	public CompletableFuture<Response> send(HttpRequest request);

	public CompletableFuture<HttpSocket> closeSocket();
}
