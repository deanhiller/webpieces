package org.webpieces.webserver.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.RouterService;

public class WebpiecesStreamHandle implements HttpStream {

	private RouterService routingService;
	private CompletableFuture<StreamWriter> future;

	public WebpiecesStreamHandle(RouterService routingService) {
		this.routingService = routingService;
	}

	@Override
	public CompletableFuture<StreamWriter> incomingRequest(Http2Request headers, ResponseStream stream) {
		RouterStreamHandle handler = new RouterResponseHandlerImpl(stream);
		future = routingService.incomingRequest(headers, handler);
		return future;
	}

	@Override
	public CompletableFuture<Void> incomingCancel(CancelReason c) {
		return future.thenCompose(writer -> writer.processPiece(c));
	}

}
