package org.webpieces.nio.api.exceptions;

public class RuntimeInterruptedException extends RuntimeException {

	private static final long serialVersionUID = 755679221931062373L;

	public RuntimeInterruptedException() {
		super();
	}

	public RuntimeInterruptedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RuntimeInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuntimeInterruptedException(String message) {
		super(message);
	}

	public RuntimeInterruptedException(Throwable cause) {
		super(cause);
	}

}
