package com.webpieces.http2.api.streaming;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.webpieces.http2.api.dto.lowlevel.CancelReason;

public class MyStreamRef implements StreamRef {

	private AtomicReference<CompletableFuture<StreamWriter>> ref = new AtomicReference<CompletableFuture<StreamWriter>>();

	public MyStreamRef(CompletableFuture<StreamWriter> writer) {
		ref.set(writer);
	}
	
	@Override
	public CompletableFuture<StreamWriter> getWriter() {
		return ref.get();
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason reason) {
		//swap out writer for a cancelled one
		CompletableFuture<StreamWriter> writer = new CompletableFuture<StreamWriter>();
		writer.completeExceptionally(new CancellationException("Cancelled.  reason="+reason));
		ref.set(writer);
		
		return CompletableFuture.completedFuture(null);
	}

}
