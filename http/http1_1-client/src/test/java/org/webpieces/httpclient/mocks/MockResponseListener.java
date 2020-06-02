package org.webpieces.httpclient.mocks;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class MockResponseListener implements HttpResponseListener {

	@Override
	public CompletableFuture<HttpDataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
		return null;
	}

	@Override
	public void failure(Throwable e) {
	}

}
