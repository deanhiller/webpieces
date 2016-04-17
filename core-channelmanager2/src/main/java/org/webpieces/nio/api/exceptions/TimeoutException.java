package org.webpieces.nio.api.exceptions;

public class TimeoutException extends RuntimeException {

	private static final long serialVersionUID = -2327080029031689941L;

	public TimeoutException() {
	}

	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}
