package org.webpieces.router.api.exceptions;

public class CookieTooLargeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CookieTooLargeException() {
		super();
		
	}

	public CookieTooLargeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public CookieTooLargeException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public CookieTooLargeException(String message) {
		super(message);
		
	}

	public CookieTooLargeException(Throwable cause) {
		super(cause);

	}

}
