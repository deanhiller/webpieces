package org.webpieces.frontend.api.exception;

public class HttpServerException extends HttpException {

	private static final long serialVersionUID = 1L;

	public HttpServerException() {
		super();
	}

	public HttpServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HttpServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpServerException(String message) {
		super(message);
	}

	public HttpServerException(Throwable cause) {
		super(cause);
	}
}
