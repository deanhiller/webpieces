package org.webpieces.util.acking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class AckAggregator2 {

	private static final Logger log = LoggerFactory.getLogger(AckAggregator2.class);
	
	private AtomicInteger countDown;
	private CompletableFuture<Void> future = new CompletableFuture<Void>();

	public AckAggregator2(
		int numAcksNeeded,
		int numBytesToAck,
		ByteAckTracker2 tracker
	) {
		this.countDown = new AtomicInteger(numAcksNeeded);
		future.thenApply(v -> {
			tracker.ackBytes(numBytesToAck);
			return null;
		});
	}

	public <T> T ack(T result, Throwable t) {
		if(t != null) {//on exception just ack the whole thing(as all others quite possibly will not ack at all)
			log.error("Exception", t);
			future.complete(null);
		}
		
		int counter = countDown.decrementAndGet();
		if(counter == 0) {
			future.complete(null);
		}
		return result;
	}

}
