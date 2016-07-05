package org.webpieces.frontend.api.exception;

import org.webpieces.httpparser.api.dto.KnownStatusCode;

public class HttpClientException extends HttpException {

	private static final long serialVersionUID = 1L;
	
	public HttpClientException(KnownStatusCode statusCode) {
		super(statusCode);
	}

	public HttpClientException(String message, KnownStatusCode statusCode, Throwable cause) {
		super(message, statusCode, cause);
	}

	public HttpClientException(String message, KnownStatusCode statusCode) {
		super(message, statusCode);
	}

	public HttpClientException(Throwable cause, KnownStatusCode statusCode) {
		super(cause, statusCode);
	}

}
