package org.webpieces.router.api.exceptions;

public class BadCookieException extends RuntimeException {

	private static final long serialVersionUID = 7804145831639203745L;

	public BadCookieException() {
		super();
	}

	public BadCookieException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BadCookieException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadCookieException(String message) {
		super(message);
	}

	public BadCookieException(Throwable cause) {
		super(cause);
	}

}
