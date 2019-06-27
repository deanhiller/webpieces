package org.webpieces.util.acking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class AckAggregator {

	private static final Logger log = LoggerFactory.getLogger(AckAggregator.class);
	
	private AtomicInteger countDown;
	private CompletableFuture<Void> future = new CompletableFuture<Void>();

	public AckAggregator(
		int numAcksNeeded,
		int numBytesToAck,
		ByteAckTracker tracker
	) {
		this.countDown = new AtomicInteger(numAcksNeeded);
		future.thenApply(v -> {
			tracker.ackBytes(numBytesToAck);
			return null;
		});
	}

	public <T> T ack(T result, Throwable t) {
		if(t != null) {//on exception just ack the whole thing(as 
			//all others quite possibly will not ack at all)
			//This is very confusing and we seem to log at least one case already.
			//we MAY lose visibility into other cases...ick...not sure..hard to see completely
			log.error("Exception should have been logged above.  If not, enable trace logs");
			log.trace(() -> "Exception", new RuntimeException(t));
			future.complete(null);
		}
		
		int counter = countDown.decrementAndGet();
		if(counter == 0) {
			future.complete(null);
		}
		return result;
	}

}
