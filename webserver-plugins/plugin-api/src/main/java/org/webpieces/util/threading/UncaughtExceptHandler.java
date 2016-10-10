package org.webpieces.util.threading;

import java.lang.Thread.UncaughtExceptionHandler;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class UncaughtExceptHandler implements UncaughtExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(UncaughtExceptHandler.class);
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Uncaught exception on thread="+t.getName(), e);
	}

}
