package org.webpieces.httpfrontend.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

class RequestListenerForTest implements RequestListener {

	private boolean isClosed;

	@Override
	public void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender) {
	}

	@Override
	public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void incomingTrailer(List<Http2Header> headers, RequestId id, boolean isComplete, ResponseSender sender) {
	}

	@Override
	public void incomingError(HttpException exc, HttpSocket channel) {
	}

	@Override
	public void clientOpenChannel(HttpSocket HttpSocket) {
	}
	
	@Override
	public void channelClosed(HttpSocket httpSocket, boolean browserClosed) {
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
