package org.playorm.nio.api.testutil;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class HandlerForTests extends Handler {

	private LogRecord failure;
	private static HandlerForTests currentHandler;
	
	private HandlerForTests() {
		this.setLevel(Level.WARNING);
	}
	
	@Override
	public void publish(LogRecord record) {
		if(!isLoggable(record))
			return;

		if(failure == null) {
			failure = record;
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}

	public static void setupLogging() {
		if(currentHandler != null)
			Logger.getLogger("").removeHandler(currentHandler);
		currentHandler = new HandlerForTests();
		Logger.getLogger("").addHandler(currentHandler);
	}
	/**
	 * Return the message in the first warning, or null if no warning
	 */
	public static void checkForWarnings() {
		currentHandler.checkForWarningsImpl();
		Logger.getLogger("").removeHandler(currentHandler);
		currentHandler = null;		
	}
	
	private void checkForWarningsImpl() {
		if(failure != null) {
			SimpleFormatter form = new SimpleFormatter();
			String log = form.format(failure);
			throw new LogHasWarningException("Log contains warning, or errors.  record=\n"+log, failure.getMessage());
		}
	}

}
