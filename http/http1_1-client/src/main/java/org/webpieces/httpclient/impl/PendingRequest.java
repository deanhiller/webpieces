package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.HttpChunkWriter;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;

public class PendingRequest {

	private HttpRequest request;
	private ResponseListener listener;
	private CompletableFuture<HttpChunkWriter> future;

	public PendingRequest(CompletableFuture<HttpChunkWriter> future, HttpRequest request, ResponseListener listener) {
		this.future = future;
		this.request = request;
		this.listener = listener;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public ResponseListener getListener() {
		return listener;
	}

	public CompletableFuture<HttpChunkWriter> getFuture() {
		return future;
	}

}
