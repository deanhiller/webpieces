package com.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Future Permit Queue that auto releases when another Future completes.  Unlike the more advanced
 * PermitQueue, you do not need to release permits
 */
public class FuturePermitQueue {

	private PermitQueue queue;
	
	public FuturePermitQueue(int numPermits) {
		queue = new PermitQueue(numPermits);
	}
	
	public <RESP> CompletableFuture<RESP> runRequest(Supplier<CompletableFuture<RESP>> processor) {
		return queue.runRequest(processor).handle((v, e) -> release(v, e))
				.thenCompose(Function.identity());
	}

	private <RESP> CompletableFuture<RESP> release(RESP v, Throwable e) {
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
