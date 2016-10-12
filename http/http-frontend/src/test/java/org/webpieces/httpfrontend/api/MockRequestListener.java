package org.webpieces.httpfrontend.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpparser.api.dto.HttpRequest;

import java.util.concurrent.CompletableFuture;

public class MockRequestListener implements RequestListener {

	private boolean isClosed;

	@Override
	public CompletableFuture<RequestId> incomingRequest(HttpRequest req, boolean isComplete, ResponseSender sender) {
		return CompletableFuture.completedFuture(new RequestId(0));
	}

	@Override
	public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void incomingError(HttpException exc, ResponseSender channel) {
	}

	@Override
	public void clientOpenChannel() {
	}
	
	@Override
	public void clientClosedChannel() {
		isClosed = true;
	}

	@Override
	public void applyWriteBackPressure(ResponseSender sender) {
	}

	@Override
	public void releaseBackPressure(ResponseSender sender) {
	}

	public boolean isClosed() {
		return isClosed;
	}

}
