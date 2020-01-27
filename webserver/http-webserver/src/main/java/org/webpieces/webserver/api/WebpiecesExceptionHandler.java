package org.webpieces.webserver.api;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebpiecesExceptionHandler implements UncaughtExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(WebpiecesExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("VERY VERY Bad, your thread is NOW DEAD and won't run!!\n"
				+ "Install a try catch if you need to keep your thread alive.\n"
				+ "Uncaught Exception. thread="+t.getName(), e);
	}

}
