package org.webpieces.util.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private AtomicInteger count = new AtomicInteger(0);
	private String threadGrpName;
	
	public NamedThreadFactory(String threadGrpName) {
		this.threadGrpName = threadGrpName;
	}

	private int getNextCount() {
		return count.getAndIncrement();
	}
	
	@Override
	public Thread newThread(Runnable r) {
		ThreadGroup grp = new ThreadGroup(threadGrpName);
		grp.setDaemon(true);
		Thread t = new Thread(grp, r, threadGrpName +getNextCount());
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(new UncaughtExceptHandler());
		return t;
	}

}
