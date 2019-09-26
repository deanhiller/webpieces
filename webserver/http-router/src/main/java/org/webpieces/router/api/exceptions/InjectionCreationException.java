package org.webpieces.router.api.exceptions;

public class InjectionCreationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InjectionCreationException() {
	}

	public InjectionCreationException(String message) {
		super(message);
	}

	public InjectionCreationException(Throwable cause) {
		super(cause);
	}

	public InjectionCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InjectionCreationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
