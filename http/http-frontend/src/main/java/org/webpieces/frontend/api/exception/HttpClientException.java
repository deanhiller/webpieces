package org.webpieces.frontend.api.exception;

public class HttpClientException extends HttpException {

	private static final long serialVersionUID = 1L;
	
	public HttpClientException() {
		super();
	}

	public HttpClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HttpClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpClientException(String message) {
		super(message);
	}

	public HttpClientException(Throwable cause) {
		super(cause);
	}

}
