package org.playorm.nio.test;

public class PerfTimer {

	private long before;
	private long after;
	
	public void start() {
		before = System.currentTimeMillis();
	}
	
	public long stop() {
		after = System.currentTimeMillis();
		return after - before;
	}
	
}
