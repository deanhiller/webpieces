package org.webpieces.router.api.exceptions;

public class DataMismatchException extends RuntimeException {

	private static final long serialVersionUID = -6676323854181254270L;

	public DataMismatchException() {
	}

	public DataMismatchException(String message) {
		super(message);
	}

	public DataMismatchException(Throwable cause) {
		super(cause);
	}

	public DataMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataMismatchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
