package org.webpieces.frontend.api.exception;

import org.webpieces.httpparser.api.dto.KnownStatusCode;

public class HttpServerException extends HttpException {

	private static final long serialVersionUID = 1L;

	public HttpServerException(KnownStatusCode statusCode) {
		super(statusCode);
	}

	public HttpServerException(String message, KnownStatusCode statusCode, Throwable cause) {
		super(message, statusCode, cause);
	}

	public HttpServerException(String message, KnownStatusCode statusCode) {
		super(message, statusCode);
	}

	public HttpServerException(Throwable cause, KnownStatusCode statusCode) {
		super(cause, statusCode);
	}
}
