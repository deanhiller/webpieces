package org.webpieces.router.api.exceptions;

public class IllegalReturnValueException extends RuntimeException {

	private static final long serialVersionUID = -2996758528390443016L;

	public IllegalReturnValueException() {
		super();
	}

	public IllegalReturnValueException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalReturnValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalReturnValueException(String message) {
		super(message);
	}

	public IllegalReturnValueException(Throwable cause) {
		super(cause);
	}
	
}
