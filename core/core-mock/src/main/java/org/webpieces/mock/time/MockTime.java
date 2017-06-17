package org.webpieces.mock.time;

import org.webpieces.util.time.Time;

public class MockTime implements Time {

	//we use a nice round number for debugging
	private long currentNanos = 1400000000*1000000;
	
	public MockTime(boolean frozenToNiceNanos) {
		if(!frozenToNiceNanos) {
			//freeze to a nice date instead
			//Epoch timestamp: 1483264800
			//Timestamp in milliseconds: 1483264800000
			//Human time (GMT): Sun, 01 Jan 2017 10:00:00 GMT
			currentNanos = 1483264800*1000000;
		}
	}

	public MockTime(long frozenTimeNanos) {
		this.currentNanos = frozenTimeNanos;
	}
	
	@Override
	public long currentMillis() {
		return currentNanos / 1000000;
	}

	@Override
	public long currentNanos() {
		return currentNanos;
	}

	public void advanceBySeconds(int seconds) {
		advanceByMilliseconds(seconds*1000);
	}
	
	public void advanceByMilliseconds(int millis) {
		currentNanos += millis*1000000;
	}
}
