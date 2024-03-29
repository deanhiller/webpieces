package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class ServiceUnavailableException extends HttpServerErrorException {

	private static final long serialVersionUID = 7804145831639203745L;

	public ServiceUnavailableException() {
		super(StatusCode.HTTP_503_SERVICE_UNAVAILABLE);
	}

	public ServiceUnavailableException(String message, Throwable cause) {
		super(StatusCode.HTTP_503_SERVICE_UNAVAILABLE, message, cause);
	}

	public ServiceUnavailableException(String message) {
		super(StatusCode.HTTP_503_SERVICE_UNAVAILABLE, message);
	}

	public ServiceUnavailableException(Throwable cause) {
		super(StatusCode.HTTP_503_SERVICE_UNAVAILABLE, cause);
	}

}
