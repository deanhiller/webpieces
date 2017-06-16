package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AckAggregator {

	private CompletableFuture<Void> future = new CompletableFuture<Void>();
	private AtomicInteger countDown;

	public AckAggregator(int numTimes) {
		this.countDown = new AtomicInteger(numTimes);
	}
	
	public <T> T ack(T result, Throwable t) {
		if(t != null) //on exception just ack the whole thing(as all others quite possibly will not ack at all)
			future.completeExceptionally(t);
		
		int counter = countDown.decrementAndGet();
		if(counter == 0) {
			future.complete(null);
		}
		return result;
	}

	public CompletableFuture<Void> getFuture() {
		return future;
	}

}
