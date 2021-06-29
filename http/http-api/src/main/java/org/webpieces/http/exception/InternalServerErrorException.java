package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class InternalServerErrorException extends HttpServerErrorException {

	private static final long serialVersionUID = 7804145831639203745L;

	public InternalServerErrorException() {
		super(StatusCode.HTTP_500_INTERNAL_SERVER_ERROR);
	}

	public InternalServerErrorException(String message, Throwable cause) {
		super(StatusCode.HTTP_500_INTERNAL_SERVER_ERROR, message, cause);
	}

	public InternalServerErrorException(String message) {
		super(StatusCode.HTTP_500_INTERNAL_SERVER_ERROR, message);
	}

	public InternalServerErrorException(Throwable cause) {
		super(StatusCode.HTTP_500_INTERNAL_SERVER_ERROR, cause);
	}

}
