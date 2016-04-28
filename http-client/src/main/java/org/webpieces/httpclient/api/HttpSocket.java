package org.webpieces.httpclient.api;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpSocket {

	public CompletableFuture<HttpSocket> connect(SocketAddress addr);
	
	public CompletableFuture<HttpResponse> send(HttpRequest request);

	public CompletableFuture<HttpSocket> closeSocket();
}
