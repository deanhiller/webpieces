package org.webpieces.util.threading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class SessionExecutorImpl implements SessionExecutor {

	private static final Logger log = LoggerFactory.getLogger(SessionExecutorImpl.class);
	private Executor executor;
	private Map<Object, List<Runnable>> cachedRunnables = new HashMap<>();
	private int counter;
	private boolean runInline = false;
	private Set<Object> currentlyRunning = new HashSet<>();

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
			
			if(counter >= 10000) //TODO: externalize this setting
				runInline = true; //switch on running inline for quite a while until threadpool catches up
			else if(counter <= 500)
				runInline = false; //switch off running inline as we are pretty much caught up
		}

		if(runInline) {
			new RunnableWithKey(key, r).run();
		} else {
			executor.execute(new RunnableWithKey(key, r));
		}
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
		
		if(runInline)
			nextRunnable.run();
		else
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
