package org.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

/**
 * This allows you to do an asynchronous synchronization block that does NOT block the thread and instead resolves the
 * future when the block is finished running
 *
 * Instead of 
 *   synchronization(object) {
 *      //do something
 *   }
 *   
 *  where the thread blocks, you can do this
 *  
 *  CompletableFuture<Void> future = asyncLock.runRequest( () -> {
 *  			//do something
 *				return null;
 *			});
 * 
 * and the thread will run immediately resolving the future or return and do other stuff until the block can run
 * 
 * One bad thing at the moment is you CANNOT nest this lock with itself as it will deadlock waiting for itself to release
 */
public class AsyncLock {

	private static final Logger log = LoggerFactory.getLogger(AsyncLock.class);
	private PermitQueue queue;
	private String logId;
	private AtomicLong counter = new AtomicLong(0);
	
	public AsyncLock() {
		this("(noId)");
	}
	
	public AsyncLock(String logId) {
		this(logId, 1000, 5);
	}

	public AsyncLock(String logId, int timeMsWarning, int queuedBackupWarnThreshold) {
		this.logId = logId;
		this.queue = new PermitQueue(logId, 1, timeMsWarning, queuedBackupWarnThreshold);
	}
	
	public <RESP> CompletableFuture<RESP> synchronizeD(Supplier<RESP> processor) {
		String key = logId+counter.getAndIncrement();
		
		Supplier<CompletableFuture<RESP>> proxy = new Supplier<CompletableFuture<RESP>>() {
			public CompletableFuture<RESP> get() {
				log.trace(() -> "key:"+key+" start virtual single thread. ");
				try {
					RESP resp = processor.get();
					return CompletableFuture.completedFuture(resp);
				} catch (Throwable e) {
					CompletableFuture<RESP> future = new CompletableFuture<>();
					future.completeExceptionally(e);
					return future;
				}
			}
		};
		
		log.trace(() -> "key:"+key+" get virtual thread or wait");
		return queue.runRequest(proxy)
				.handle((v, e) -> {
					return release(v, e, key);
				})
				.thenCompose(Function.identity());
	}

	private <RESP> CompletableFuture<RESP> release(RESP v, Throwable e, String key) {
		log.trace(() -> "key:"+key+" end virtual single thread");
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
