package org.webpieces.util.acking;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AckAggregator {

	private static final Logger log = LoggerFactory.getLogger(AckAggregator.class);
	
	private AtomicInteger countDown;
	private XFuture<Void> future = new XFuture<>();

	public AckAggregator(
		int numFuturesToResolveOnceBytesAreAcked,
		int numBytesToAck,
		ByteAckTracker tracker
	) {
		this.countDown = new AtomicInteger(numFuturesToResolveOnceBytesAreAcked);
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
			//You can also turn on trace logs of this package or class to see the exception
			log.error("Exception should have been logged above.  If not, enable trace logs");
			if(log.isTraceEnabled())
				log.trace("Exception", new RuntimeException(t));
			future.complete(null);
		}
		
		int counter = countDown.decrementAndGet();
		if(counter == 0) {
			future.complete(null);
		}
		return result;
	}

}
