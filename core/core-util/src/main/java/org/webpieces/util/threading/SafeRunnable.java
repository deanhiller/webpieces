package org.webpieces.util.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeRunnable implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SafeRunnable.class);
	
	private Runnable runnable;
	
	public SafeRunnable(Runnable runnable) {
		this.runnable = runnable;
	}
	
	@Override
	public void run() {
		try {
			runnable.run();
			
		} catch(Throwable e) {
			log.error("Exception running runnable", e);
		}
		
	}

}
