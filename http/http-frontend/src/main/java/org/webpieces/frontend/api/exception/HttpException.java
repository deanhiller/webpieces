package org.webpieces.frontend.api.exception;

public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HttpException() {
		super();
	}

	public HttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

}
