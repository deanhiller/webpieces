package org.webpieces.webserver.api;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebpiecesExceptionHandler implements UncaughtExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(WebpiecesExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if("main".equals(t.getName())) {
			log.error("Your main threaad jsut died due to an exception", e);
			return;
		}
		
		log.error("This could be VERY Bad, your thread may have died DEAD and won't run!!\n"
				+ "Install a try catch to not see this last ditch effor of a warning.\n"
				+ "Uncaught Exception. thread="+t.getName(), e);
	}

}
