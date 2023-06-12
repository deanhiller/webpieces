package org.webpieces.util.threading;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UncaughtExceptHandler implements UncaughtExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(UncaughtExceptHandler.class);
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("THREAD DYING: Uncaught exception on thread ( you really should try..catch stuff!! ) ="+t.getName(), e);
	}

}
