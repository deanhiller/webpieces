package org.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Future Permit Queue that auto releases when another Future completes.  Unlike the more advanced
 * PermitQueue, you do not need to release permits
 */
public class FuturePermitQueue {

	private static final Logger log = LoggerFactory.getLogger(FuturePermitQueue.class);
	private PermitQueue queue;
	private String key;
	
	public FuturePermitQueue(String key, int numPermits) {
		this.key = key;
		queue = new PermitQueue(numPermits);
	}
	
	public <RESP> CompletableFuture<RESP> runRequest(Supplier<CompletableFuture<RESP>> processor) {
		Supplier<CompletableFuture<RESP>> proxy = new Supplier<CompletableFuture<RESP>>() {
			public CompletableFuture<RESP> get() {
				if(log.isDebugEnabled())
					log.debug("key:"+key+" start virtual single thread. ");
				CompletableFuture<RESP> fut = processor.get();
				if(log.isDebugEnabled())
					log.debug("key:"+key+" halfway there.  future needs to be acked to finish work and release virtual thread");
				return fut;
			}
		};
		
		if(log.isDebugEnabled())
			log.debug("key:"+key+" get virtual thread or wait");
		return queue.runRequest(proxy)
				.handle((v, e) -> {
					return release(v, e);
				})
				.thenCompose(Function.identity());
	}

	private <RESP> CompletableFuture<RESP> release(RESP v, Throwable e) {
		if(log.isDebugEnabled())
			log.debug("key:"+key+" end virtual single thread");
		//immediately release when future is complete
		queue.releasePermit();

    	CompletableFuture<RESP> future = new CompletableFuture<RESP>();
        if (e != null) {
        	future.completeExceptionally(e);
        } else
        	future.complete(v);
        
        return future;
	}
	
}
