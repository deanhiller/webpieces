package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.RequestId;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.nio.api.channels.Channel;

import java.util.concurrent.CompletableFuture;

public class PendingRequest {

	private HttpRequest request;
	private boolean isComplete;
	private ResponseListener listener;
	private CompletableFuture<RequestId> future;

	public PendingRequest(HttpRequest request, boolean isComplete, ResponseListener listener, CompletableFuture<RequestId> future) {
		this.request = request;
		this.isComplete = isComplete;
		this.listener = listener;
		this.future = future;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public ResponseListener getListener() {
		return listener;
	}

	public void complete(RequestId id) {
		future.complete(id);
	}
	
}
