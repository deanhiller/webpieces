package org.webpieces.util.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private AtomicInteger count = new AtomicInteger(0);
	private String threadNamePrefix;
	
	public NamedThreadFactory(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	private int getNextCount() {
		return count.getAndIncrement();
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, threadNamePrefix+getNextCount());
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(new UncaughtExceptHandler());
		return t;
	}

}
