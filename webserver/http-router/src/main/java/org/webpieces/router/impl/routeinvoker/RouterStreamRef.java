package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class RouterStreamRef implements StreamRef, Function<CancelReason, CompletableFuture<Void>> {

	private CompletableFuture<StreamWriter> writer;
	private Function<CancelReason, CompletableFuture<Void>> cancelFunc;
	private String id; //can be used in tracing/debugging as it's hard to realize which ref came from where

	public RouterStreamRef(String id, CompletableFuture<StreamWriter> writer, Function<CancelReason, CompletableFuture<Void>> cancelFunc) {
		this.id = id;
		this.writer = writer;
		this.cancelFunc = cancelFunc;
	}

	public RouterStreamRef(String id) {
		this.id = id;
		this.writer = CompletableFuture.completedFuture(new NullWriter());
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
	
	public RouterStreamRef thenApply(String id, Function<StreamWriter, StreamWriter> fn) {
		CompletableFuture<StreamWriter> newWriter = writer.thenApply(fn);
		return new RouterStreamRef(id, newWriter, this);
	}

	@Override
	public CompletableFuture<Void> apply(CancelReason t) {
		return cancel(t);
	}

}
