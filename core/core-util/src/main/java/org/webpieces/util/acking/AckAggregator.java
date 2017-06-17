package org.webpieces.util.acking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AckAggregator {

	private AtomicInteger countDown;
	private CompletableFuture<Void> ackForBytePayload;
	private CompletableFuture<Void> allAcksReceived;

	public AckAggregator(
		CompletableFuture<Void> ackForBytePayload, 
		int numAcksNeeded,
		CompletableFuture<Void> allAcksReceived
	) {
		this.ackForBytePayload = ackForBytePayload;
		this.countDown = new AtomicInteger(numAcksNeeded);
		this.allAcksReceived = allAcksReceived;
	}

	public <T> T ack(T result, Throwable t) {
		if(t != null) //on exception just ack the whole thing(as all others quite possibly will not ack at all)
			allAcksReceived.completeExceptionally(t);
		
		int counter = countDown.decrementAndGet();
		if(counter == 0) {
			allAcksReceived.complete(null);
		}
		return result;
	}

	public CompletableFuture<Void> getAckBytePayloadFuture() {
		return ackForBytePayload;
	}

}
