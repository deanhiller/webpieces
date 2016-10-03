package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class CompletableListener implements ResponseListener {

	private CompletableFuture<HttpResponse> future;
	private boolean ignoreIsComplete;

	public CompletableListener(CompletableFuture<HttpResponse> future) {
		this.future = future;
		this.ignoreIsComplete = false;
	}

	/**
	 *
	 * @param future
	 * @param ignoreIsComplete if true, completes the future after just getting the first HttpResponse 'incomingResponse'
	 *
	 */
	public CompletableListener(CompletableFuture<HttpResponse> future, boolean ignoreIsComplete) {
		this.future = future;
		this.ignoreIsComplete = ignoreIsComplete;
	}

	@Override
	public void incomingResponse(HttpResponse resp, boolean isComplete) {
		if(!isComplete && !ignoreIsComplete) {
			future.completeExceptionally(new IllegalStateException("You need to call "
					+ "sendRequest(HttpRequest req, ResponseListener l) because this is a "
					+ "chunked download response and could potentially blow out your memory"));
		}
		future.complete(resp);
	}

	@Override
	public void incomingResponse(HttpResponse resp, HttpRequest req, boolean isComplete) {
		// This listener ignores the request associated with the response
		incomingResponse(resp, isComplete);
	}

	@Override
	public void incomingData(DataWrapper data, boolean isLastData) {
	}

	@Override
	public void incomingData(DataWrapper data, HttpRequest request, boolean isLastData) {
	}

	@Override
	public void failure(Throwable e) {
		future.completeExceptionally(e);
	}

}
