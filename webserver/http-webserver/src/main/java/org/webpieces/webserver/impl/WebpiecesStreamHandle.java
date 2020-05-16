package org.webpieces.webserver.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.router.api.RouterResponseHandler;
import org.webpieces.router.api.RouterService;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class WebpiecesStreamHandle implements HttpStream {

	private RouterService routingService;
	private CompletableFuture<StreamWriter> future;

	public WebpiecesStreamHandle(RouterService routingService) {
		this.routingService = routingService;
	}

	@Override
	public CompletableFuture<StreamWriter> incomingRequest(Http2Request headers, ResponseStream stream) {
		RouterResponseHandler handler = new RouterResponseHandlerImpl(stream);
		future = routingService.incomingRequest(headers, handler);
		return future;
	}

	@Override
	public CompletableFuture<Void> incomingCancel(CancelReason c) {
		return future.thenCompose(writer -> writer.processPiece(c));
	}

}
