package org.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

/**
 * An asynchronous queue that concurrently runs the number of operations allowed(#permits operations) and adds any others into a 
 * queue until the previous finish returning to the caller to do other operations
 */
public class PermitQueue {

	private static final Logger log = LoggerFactory.getLogger(PermitQueue.class);
	@SuppressWarnings("rawtypes")
	private final ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue<>();
	private final Semaphore permits;
	private final AtomicInteger toBeRemoved = new AtomicInteger(0);
	private int permitCount;
	private int timeMsWarning;
	private int queuedBackupWarnThreshold;
	private AtomicLong counter = new AtomicLong(0);
	private String logId;

	public PermitQueue(int numPermits) {
		this("(noId)", numPermits, 3000, 1000);
	}
	
	public PermitQueue(String logId, int numPermits, int timeMsWarning, int queuedBackupWarnThreshold) {
		this.logId = logId;
		this.permitCount = numPermits;
		this.timeMsWarning = timeMsWarning;
		this.queuedBackupWarnThreshold = queuedBackupWarnThreshold;
		permits = new Semaphore(numPermits);
	}
	
	
	@SuppressWarnings("unchecked")
	public <RESP> CompletableFuture<RESP> runRequest(Supplier<CompletableFuture<RESP>> processor) {
		String key = logId+counter.getAndIncrement();

		long time = System.currentTimeMillis();
		CompletableFuture<RESP> future = new CompletableFuture<RESP>();
		queue.add(new QueuedRequest<RESP>(future, processor, time));

		//take a peek at the first item in queue and see when it was queued
		QueuedRequest<RESP> item = (QueuedRequest<RESP>) queue.peek();
		long timeQueued = item.getTimeQueued();
		long timeDelayed = time - timeQueued;
		if(timeDelayed > timeMsWarning)
			log.warn("id:"+key+" Your PermitQueue/Lock has the first item in the queue waiting "+timeDelayed+"ms so you may have deadlock or just a very contentious lock(you probably should look into this)");		
		if(backupSize() > queuedBackupWarnThreshold)
			log.warn("id:"+key+" Your lock is backing up with requests.  either too much contention or deadlock occurred(either way, you should fix this)");

		processItemFromQueue();
		
		return future;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processItemFromQueue() {
		boolean acquired = permits.tryAcquire();
		if(!acquired) 
			return;
		
		QueuedRequest req = (QueuedRequest) queue.poll();
		if(req == null) {
			releaseSinglePermit(); //release acquired permit
			return;
		}

		CompletableFuture<Object> future = req.getFuture();
		
		try {
			CompletableFuture<Object> resp = (CompletableFuture<Object>) req.getProcessor().get();
			resp.handle((r, t) -> handle(r, t, future));
		} catch(Throwable e) {
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

	private Void handle(Object resp, Throwable t, CompletableFuture<Object> future) {
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
			log.info(logId+"increasing permits in pool by "+permitCnt);
			//apply the release now that the function is RUN WHEN the client resolves the release future
			permits.release(permitCnt);
			
			for(int i = 0; i < permitCnt; i++) {
				processItemFromQueue();
			}
		} else {
			log.info(logId+"decreasing permits in pool by "+permitCnt);
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

	public int backupSize() {
		return queue.size();
	}

}
