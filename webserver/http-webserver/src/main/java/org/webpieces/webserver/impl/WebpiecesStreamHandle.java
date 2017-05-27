package org.webpieces.webserver.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.ServerSocketInfo;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.RstStreamFrame;

public class WebpiecesStreamHandle implements StreamHandle {

	private RequestHelpFacade facade;
	private FrontendStream stream;
	private RequestStreamWriter writer;

	public WebpiecesStreamHandle(RequestHelpFacade facade, FrontendStream stream) {
		this.facade = facade;
		this.stream = stream;
		
	}

	@Override
	public CompletableFuture<StreamWriter> process(Http2Request headers) {
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
