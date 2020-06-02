package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class RouterStreamRef implements StreamRef {

	private CompletableFuture<StreamWriter> writer;
	private Function<CancelReason, CompletableFuture<Void>> cancelFunc;

	public RouterStreamRef(CompletableFuture<StreamWriter> writer, Function<CancelReason, CompletableFuture<Void>> cancelFunc) {
		this.writer = writer;
		this.cancelFunc = cancelFunc;
	}

	@Override
	public CompletableFuture<StreamWriter> getWriter() {
		return writer;
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason reason) {
		if(cancelFunc != null)
			return cancelFunc.apply(reason);
		
		return CompletableFuture.completedFuture(null);
	}
	
	public RouterStreamRef thenApply() {
		
	}

}
