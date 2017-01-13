package com.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class QueuedRequest<RESP> {

	private CompletableFuture<RESP> future;
	private Supplier<CompletableFuture<RESP>> processor;

	public QueuedRequest(
			CompletableFuture<RESP> future, 
			Supplier<CompletableFuture<RESP>> processor
			) {
		this.future = future;
		this.processor = processor;
	}

	public CompletableFuture<RESP> getFuture() {
		return future;
	}

	public Supplier<CompletableFuture<RESP>> getProcessor() {
		return processor;
	}

}
