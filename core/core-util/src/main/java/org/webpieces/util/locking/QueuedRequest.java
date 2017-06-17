package org.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class QueuedRequest<RESP> {

	private CompletableFuture<RESP> future;
	private Supplier<CompletableFuture<RESP>> processor;
	private long timeQueued;

	public QueuedRequest(
			CompletableFuture<RESP> future, 
			Supplier<CompletableFuture<RESP>> processor, 
			long timeQueued
	) {
		this.future = future;
		this.processor = processor;
		this.timeQueued = timeQueued;
	}

	public CompletableFuture<RESP> getFuture() {
		return future;
	}

	public Supplier<CompletableFuture<RESP>> getProcessor() {
		return processor;
	}

	public long getTimeQueued() {
		return timeQueued;
	}

}
