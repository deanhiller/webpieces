package org.webpieces.util.threading;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionExecutorImplNew implements SessionExecutor {

	private static final Logger log = LoggerFactory.getLogger(SessionExecutorImplNew.class);
	private Executor executor;
	private ConcurrentMap<Object, List<Runnable>> cachedRunnables = new ConcurrentHashMap<>();
	private int counter;
	//using as a set for concurrency that is compare and set type
	private ConcurrentMap<Object, Boolean> currentlyRunning = new ConcurrentHashMap<>();

	public SessionExecutorImplNew(Executor executor) {
		this.executor = executor;
	}
	
	@Override
	public <T> XFuture<T> executeCall(Object key, Callable<XFuture<T>> callable) {
		XFuture<T> future = new XFuture<T>();
		FutureRunnable<T> r = new FutureRunnable<>(callable, future);

		execute(key, r);
		
		return future;
	}
	
	private class FutureRunnable<T> implements Runnable {
		private Callable<XFuture<T>> callable;
		private XFuture<T> future;

		public FutureRunnable(Callable<XFuture<T>> callable, XFuture<T> future) {
			this.callable = callable;
			this.future = future;
		}

		@Override
		public void run() {
			try {
				XFuture<T> result = callable.call();
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
		synchronized(translate(key)) {
			if(currentlyRunning.containsKey(key)) {
				cacheRunnable(key, new RunnableWithKey(key, r));
				return;
			} else {
				currentlyRunning.put(key, Boolean.TRUE);
			}
			
			if(counter >= 10000)
				log.warn("Session executor is falling behind on incoming data, possibly add back pressure", new RuntimeException());
		}

		executor.execute(new RunnableWithKey(key, r));
	}

	private void executeNext(Object key) {
		Runnable nextRunnable = null;
		synchronized (translate(key)) {
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
	
	private Object translate(Object key) {
		if(key instanceof String) {
			//strings are not the same when they are the same(ironic) but this makes them
			//the same object and same lock "hello" != "hel"+"lo"
			return ((String)key).intern();
		}
		return key;
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
				runnable.run();
			} catch(Throwable e) {
				log.error("Uncaught Exception", e);
			} finally {
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
