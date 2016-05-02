package org.webpieces.httpclient.api;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import com.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpSocket {

	public CompletableFuture<HttpSocket> connect(SocketAddress addr);
	
	public void send(HttpRequest request, ResponseListener l);

	public CompletableFuture<HttpSocket> closeSocket();
}
