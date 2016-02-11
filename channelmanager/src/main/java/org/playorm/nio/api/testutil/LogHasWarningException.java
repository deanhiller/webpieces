package org.playorm.nio.api.testutil;

public class LogHasWarningException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final String logMessage;

	public LogHasWarningException(String message, String logMsg) {
		super(message);
		this.logMessage = logMsg;
	}

	public String getLogMessage() {
		return logMessage;
	}

	
}
