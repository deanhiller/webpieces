package org.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A lock/mutex that does not block the thread and just runs it on the thread that releases the lock.  This has the potential
 * to stackoverflow(and we could modify to add an executor to prevent that), but part of me wonders if the stack overflow is
 * due to a lock that is too contentious and should be fixed(ie. design should be changed).  Therefore, I am going to wait
 * until this manifests to learn more before I solve a problem that may not exist
 */
public class AsyncLockWithRelease {

	private static final Logger log = LoggerFactory.getLogger(AsyncLockWithRelease.class);
	private PermitQueue permitQueue;
	private int queuedBackupWarnThreshold;
	private String logId;
	private AtomicInteger counter = new AtomicInteger(0);
	
	public AsyncLockWithRelease() {
		this("(noIdSet)", 1000);
	}

	public AsyncLockWithRelease(String logId, int queuedBackupWarnThreshold) {
		this.logId = logId;
		this.queuedBackupWarnThreshold = queuedBackupWarnThreshold;
		this.permitQueue = new PermitQueue(1);
	}

	public <RESP> CompletableFuture<RESP> lock(Function<Lock, CompletableFuture<RESP>> processor) {
		int id = counter.getAndIncrement();
		String key = logId+id;

		if(permitQueue.backupSize() > queuedBackupWarnThreshold)
			log.warn("id:"+key+" Your lock is backing up with requests.  either too much contention or deadlock occurred(either way, you should fix this)");
		
		Lock lock = new LockImpl(key);
		Supplier<CompletableFuture<RESP>> proxy = new Supplier<CompletableFuture<RESP>>() {
			public CompletableFuture<RESP> get() {
				log.info("key:"+key+" enter async sync block");
				CompletableFuture<RESP> fut = processor.apply(lock);
				return fut;
			}
		};
		
		log.info("key:"+key+" aboud to get lock or get queued");
		
		return permitQueue.runRequest(proxy);
	}
	
	private class LockImpl implements Lock {
		private String key;
		public LockImpl(String key) {
			this.key = key;
		}

		@Override
		public void release() {
			log.info("key:"+key+" Exit async sync block");
			permitQueue.releasePermit();
		}
	}
}
