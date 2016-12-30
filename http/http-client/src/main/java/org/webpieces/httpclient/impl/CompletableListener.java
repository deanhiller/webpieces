package org.webpieces.httpclient.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class CompletableListener implements ResponseListener {

	private CompletableFuture<HttpResponse> future;
	private boolean ignoreIsComplete;

	public CompletableListener(CompletableFuture<HttpResponse> future) {
		this.future = future;
		this.ignoreIsComplete = false;
	}

	/**
	 *
	 * @param future the future that completes when the response comes in
	 * @param ignoreIsComplete if true, completes the future after just getting the first HttpResponse 'incomingResponse'
	 *
	 */
	public CompletableListener(CompletableFuture<HttpResponse> future, boolean ignoreIsComplete) {
		this.future = future;
		this.ignoreIsComplete = ignoreIsComplete;
	}

	@Override
	public void incomingResponse(HttpResponse resp, HttpRequest req, ResponseId id, boolean isComplete) {
		// This listener ignores the request associated with the response
        if(!isComplete && !ignoreIsComplete) {
            future.completeExceptionally(new IllegalStateException("You need to call "
                    + "sendRequest(HttpRequest req, ResponseListener l) because this is a "
                    + "chunked or http2 download response and could potentially blow out your memory"));
        }
        future.complete(resp);
	}

	@Override
	public CompletableFuture<Void> incomingData(DataWrapper data, ResponseId id, boolean isLastData) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void incomingTrailer(List<Http2Header> headers, ResponseId id, boolean isComplete) {
	}

	@Override
	public void failure(Throwable e) {
		future.completeExceptionally(e);
	}

}
