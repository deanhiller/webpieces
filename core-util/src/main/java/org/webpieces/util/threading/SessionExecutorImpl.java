package org.webpieces.util.threading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionExecutorImpl implements SessionExecutor {

	private static final Logger log = LoggerFactory.getLogger(SessionExecutorImpl.class);
	private Executor executor;
	private Map<Object, List<Runnable>> cachedRunnables = new HashMap<>();
	private Set<Object> currentlyRunning = new HashSet<>();
	//private int counter = 0;

	public SessionExecutorImpl(Executor executor) {
		this.executor = executor;
	}
	
	@Override
	public void execute(Object key, Runnable r) {
		synchronized(this) {
			if(currentlyRunning.contains(key)) {
				cacheRunnable(key, new RunnableWithKey(key, r));
				return;
			} else {
				currentlyRunning.add(key);
			}
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
//			counter++;
//			if(counter % 1000 == 0)
//				log.info("list size="+list.size());
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
				runnable.run();
			} catch(Throwable e) {
				log.warn("Uncaught Exception", e);
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
	}
}
