package org.webpieces.util.threading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class SessionExecutorImpl implements SessionExecutor {

	private static final Logger log = LoggerFactory.getLogger(SessionExecutorImpl.class);
	private Executor executor;
	private Map<Object, List<Runnable>> cachedRunnables = new HashMap<>();
	private int counter;
	private Set<Object> currentlyRunning = new HashSet<>();
	private ThreadLocal<Boolean> isFromThisPool = new ThreadLocal<>();

	public SessionExecutorImpl(Executor executor) {
		this.executor = executor;
	}
	
	@Override
	public <T> CompletableFuture<T> executeCall(Object key, Callable<CompletableFuture<T>> callable) {
		CompletableFuture<T> future = new CompletableFuture<T>();
		FutureRunnable<T> r = new FutureRunnable<>(callable, future);

		execute(key, r);
		
		return future;
	}
	
	private class FutureRunnable<T> implements Runnable {
		private Callable<CompletableFuture<T>> callable;
		private CompletableFuture<T> future;

		public FutureRunnable(Callable<CompletableFuture<T>> callable, CompletableFuture<T> future) {
			this.callable = callable;
			this.future = future;
		}

		@Override
		public void run() {
			try {
				CompletableFuture<T> result = callable.call();
				result.handle((r, t) -> {
					if(t != null) {
						future.completeExceptionally(t);
					}
					future.complete(r);
					return null;
				});
			} catch(Throwable e) {
				future.completeExceptionally(e);
			}
		}
	}
	
	@Override
	public void execute(Object key, Runnable r) {
		if(isFromThisPool.get() != null) {
			//if this threadpool is dumping into this threadpool, continue to drive it through so
			//the whole chain can gc faster(better to complete requests/responses all the way through than
			//parallelize too many
			r.run();
			return;
		}
		
		synchronized(this) {
			if(currentlyRunning.contains(key)) {
				cacheRunnable(key, new RunnableWithKey(key, r));
				return;
			} else {
				currentlyRunning.add(key);
			}
			
			if(counter >= 10000)
				log.warn("Session executor is falling behind on incoming data, possibly add back pressure", new RuntimeException());
		}

		executor.execute(new RunnableWithKey(key, r));
	}

	private void executeNext(Object key) {
		Runnable nextRunnable = null;
		synchronized (this) {
			List<Runnable> list = cachedRunnables.get(key);
			if(list == null) {
				currentlyRunning.remove(key);
				return;
			}
			nextRunnable = list.remove(0);
			counter--;
			if(list.isEmpty()) {
				cachedRunnables.remove(key);
			}
		}
		
		executor.execute(nextRunnable);
	}
	
	private class RunnableWithKey implements Runnable {
		private Runnable runnable;
		private Object key;

		public RunnableWithKey(Object key, Runnable r) {
			this.key = key;
			this.runnable = r;
		}

		@Override
		public void run() {
			try {
				isFromThisPool.set(true);
				runnable.run();
			} catch(Throwable e) {
				log.error("Uncaught Exception", e);
			} finally {
				isFromThisPool.set(null);
				executeNext(key);
			}
		}
	}
	
	private synchronized void cacheRunnable(Object key, Runnable r) {
		List<Runnable> list = cachedRunnables.get(key);
		if(list == null) {
			list = new LinkedList<>();
			cachedRunnables.put(key, list);
		}
		list.add(r);
		counter++;
	}
}
