package com.webpieces.util.time;

public class TimeImpl implements Time {

	public TimeImpl() {
	}

	@Override
	public long currentMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public long currentNanos() {
		return System.nanoTime();
	}


}
