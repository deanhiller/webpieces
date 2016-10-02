package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class CompletableListener implements ResponseListener {

	private CompletableFuture<HttpResponse> future;

	public CompletableListener(CompletableFuture<HttpResponse> future) {
		this.future = future;
	}

	@Override
	public void incomingResponse(HttpResponse resp, boolean isComplete) {
		if(!isComplete) {
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
	public void incomingChunk(HttpChunk chunk, boolean isLastChunk) {
	}

	@Override
	public void failure(Throwable e) {
		future.completeExceptionally(e);
	}

}
