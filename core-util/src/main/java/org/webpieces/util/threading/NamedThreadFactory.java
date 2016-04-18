package org.webpieces.util.threading;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

	private int count = 0;
	private String threadNamePrefix;
	
	public NamedThreadFactory(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	private synchronized int getNextCount() {
		return count++;
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, threadNamePrefix+getNextCount());
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(new UncaughtExceptHandler());
		return t;
	}

}
