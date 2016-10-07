package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.nio.api.channels.Channel;

import java.util.concurrent.CompletableFuture;

public class PendingRequest {

	private HttpRequest request;
	private ResponseListener listener;
	private CompletableFuture<HttpRequest> future;

	public PendingRequest(HttpRequest request, ResponseListener listener, CompletableFuture<HttpRequest> future) {
		this.request = request;
		this.listener = listener;
		this.future = future;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public ResponseListener getListener() {
		return listener;
	}

	public void complete() {
		future.complete(request);
	}
	
}
