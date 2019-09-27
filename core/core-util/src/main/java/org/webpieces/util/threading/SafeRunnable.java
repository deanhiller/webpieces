package org.webpieces.util.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SafeRunnable implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SafeRunnable.class);
	
	@Override
	public void run() {
		try {
			runImpl();
		} catch(Throwable e) {
			log.error("Exception running runnable", e);
		}
		
	}

	protected abstract void runImpl();

}
