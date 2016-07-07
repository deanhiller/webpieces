package org.webpieces.router.impl;

public class InvokeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvokeException() {
		super();
	}

	public InvokeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvokeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvokeException(String message) {
		super(message);
	}

	public InvokeException(Throwable cause) {
		super(cause);
	}
	
}
