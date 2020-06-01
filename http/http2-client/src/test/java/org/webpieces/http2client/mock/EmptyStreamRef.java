package org.webpieces.http2client.mock;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class EmptyStreamRef implements StreamRef {

	private CompletableFuture<StreamWriter> future;

	public EmptyStreamRef(CompletableFuture<StreamWriter> future) {
		this.future = future;
	}

	@Override
	public CompletableFuture<StreamWriter> getWriter() {
		return future;
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason reason) {
		throw new UnsupportedOperationException("use different mock, this one is not for testing cancel");
	}

}
