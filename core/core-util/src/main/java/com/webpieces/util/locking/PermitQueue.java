package com.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;


public class PermitQueue<RESP> {

	private static final Logger log = LoggerFactory.getLogger(PermitQueue.class);
	private final ConcurrentLinkedQueue<QueuedRequest<RESP>> queue = new ConcurrentLinkedQueue<>();
	private final Semaphore permits;
	private final AtomicInteger toBeRemoved = new AtomicInteger(0);
	private int permitCount;
	
	public PermitQueue(int numPermits) {
		permitCount = numPermits;
		permits = new Semaphore(numPermits);
	}
	
	public CompletableFuture<RESP> runRequest(Supplier<CompletableFuture<RESP>> processor) {
		CompletableFuture<RESP> future = new CompletableFuture<RESP>();
		queue.add(new QueuedRequest<>(future, processor));

		processItemFromQueue();
		
		return future;
	}

	private void processItemFromQueue() {
		boolean acquired = permits.tryAcquire();
		if(!acquired) 
			return;
		
		QueuedRequest<RESP> req = queue.poll();
		if(req == null) {
			releaseSinglePermit(); //release acquired permit
			return;
		}

		CompletableFuture<RESP> future = req.getFuture();
		
		try {
			CompletableFuture<RESP> resp = req.getProcessor().get();
			resp.handle((r, t) -> handle(r, t, future));
		} catch(Throwable e) {
			log.warn("Exception", e);
			handle(null, e, future);
		}
	}
	
	private void releaseSinglePermit() {
		int value = toBeRemoved.decrementAndGet();
		if(value >= 0) {
			//we need to NOT release permit back into pool and just return here
			return;
		}
		
		//oops, it's less than 0, add it back now
		toBeRemoved.incrementAndGet();

		permits.release(); 
	}

	private Void handle(RESP resp, Throwable t, CompletableFuture<RESP> future) {
		if(t != null)
			future.completeExceptionally(t);
		else
			future.complete(resp);
		
		return null;
	}

	public int totalPermits() {
		return permitCount;
	}
	public int availablePermits() {
		return permits.availablePermits();
	}

	public void releasePermit() {
		//apply the release now that the function is RUN WHEN the client resolves the release future
		releaseSinglePermit();
		
		processItemFromQueue();
	}
	
	public void modifyPermitPoolSize(int permitCnt) {
		permitCount += permitCnt;
		if(permitCnt > 0) {
			log.info("increasing permits in pool by "+permitCnt);
			//apply the release now that the function is RUN WHEN the client resolves the release future
			permits.release(permitCnt);
			
			for(int i = 0; i < permitCnt; i++) {
				processItemFromQueue();
			}
		} else {
			log.info("decreasing permits in pool by "+permitCnt);
			int positiveToRemove = -permitCnt;
			//first try to remove them all immediately
			int countOfRemoved = 0;
			while(permits.tryAcquire()) {
				countOfRemoved++;
				if(countOfRemoved >= positiveToRemove)
					break;
			}
			
			int toRemoveStill = positiveToRemove - countOfRemoved;
			//then cache the rest that will get removed on release(ie. when someone is done)
			toBeRemoved.addAndGet(toRemoveStill);
		}
	}

}
