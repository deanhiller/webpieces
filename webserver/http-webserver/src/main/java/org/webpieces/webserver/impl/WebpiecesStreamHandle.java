package org.webpieces.webserver.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class WebpiecesStreamHandle implements HttpStream {

	private RequestHelpFacade facade;
	private RequestStreamWriter writer;

	public WebpiecesStreamHandle(RequestHelpFacade facade) {
		this.facade = facade;
		
	}

	@Override
	public CompletableFuture<StreamWriter> process(Http2Request headers, ResponseStream stream) {
		writer = new RequestStreamWriter(facade, stream, headers);
		
		if(headers.isEndOfStream()) {
			CompletableFuture<Void> future = writer.handleCompleteRequest();
			writer.setOutstandingRequest(future);
			return future.thenApply( v -> writer);
		}

		return CompletableFuture.completedFuture(writer);
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason c) {
		writer.cancelOutstandingRequest();
		return CompletableFuture.completedFuture(null);
	}

}
