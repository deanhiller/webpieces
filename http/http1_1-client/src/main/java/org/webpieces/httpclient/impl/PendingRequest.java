package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.HttpDataWriter;
import org.webpieces.httpclient.api.HttpResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class PendingRequest {

	private HttpRequest request;
	private HttpResponseListener listener;
	private CompletableFuture<HttpDataWriter> future;

	public PendingRequest(CompletableFuture<HttpDataWriter> future, HttpRequest request, HttpResponseListener listener) {
		this.future = future;
		this.request = request;
		this.listener = listener;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpResponseListener getListener() {
		return listener;
	}

	public CompletableFuture<HttpDataWriter> getFuture() {
		return future;
	}

}
