package org.webpieces.util.futures;

public class ComparableRunnable implements Runnable {

	public boolean wasRun = false;
	
	@Override
	public void run() {
		wasRun = true;
	}
	
}
