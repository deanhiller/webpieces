package org.webpieces.util.logging;

public class LoggerFactory {

	private static Logger getLogger(Class<?> clazz) {
		org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);
		return new Logger(logger);
	}
	
}
