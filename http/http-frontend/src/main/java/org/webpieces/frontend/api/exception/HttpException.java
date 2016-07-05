package org.webpieces.frontend.api.exception;

import org.webpieces.httpparser.api.dto.KnownStatusCode;

public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final KnownStatusCode statusCode;

	public HttpException(KnownStatusCode statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public HttpException(String message, KnownStatusCode statusCode, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;

	}

	public HttpException(String message, KnownStatusCode statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	public HttpException(Throwable cause, KnownStatusCode statusCode) {
		super(cause);
		this.statusCode = statusCode;
	}

	public KnownStatusCode getStatusCode() {
		return statusCode;
	}
	
}
