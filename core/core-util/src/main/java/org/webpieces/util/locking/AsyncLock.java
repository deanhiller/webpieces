package org.webpieces.util.locking;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *  XFuture<Void> future = asyncLock.runRequest( () -> {
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
	
	public <RESP> XFuture<RESP> synchronizeD(Supplier<RESP> processor) {
		String key = logId+counter.getAndIncrement();
		
		Supplier<XFuture<RESP>> proxy = new Supplier<XFuture<RESP>>() {
			public XFuture<RESP> get() {
				if(log.isTraceEnabled())
					log.trace("key:"+key+" start virtual single thread. ");
				try {
					RESP resp = processor.get();
					return XFuture.completedFuture(resp);
				} catch (Throwable e) {
					XFuture<RESP> future = new XFuture<>();
					future.completeExceptionally(e);
					return future;
				}
			}
		};
		
		if(log.isTraceEnabled())
			log.trace("key:"+key+" get virtual thread or wait");
		return queue.runRequest(proxy)
				.handle((v, e) -> {
					return release(v, e, key);
				})
				.thenCompose(Function.identity());
	}

	private <RESP> XFuture<RESP> release(RESP v, Throwable e, String key) {
		if(log.isTraceEnabled())
			log.trace("key:"+key+" end virtual single thread");
		//immediately release when future is complete
		queue.releasePermit();

    	XFuture<RESP> future = new XFuture<RESP>();
        if (e != null) {
        	future.completeExceptionally(e);
        } else
        	future.complete(v);
        
        return future;
	}
	
}
