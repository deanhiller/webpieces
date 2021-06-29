package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class NotFoundException extends HttpClientErrorException {

	private static final long serialVersionUID = 7804145831639203745L;

	public NotFoundException() {
		super(StatusCode.HTTP_404_NOT_FOUND);
	}

	public NotFoundException(String message, Throwable cause) {
		super(StatusCode.HTTP_404_NOT_FOUND, message, cause);
	}

	public NotFoundException(String message) {
		super(StatusCode.HTTP_404_NOT_FOUND, message);
	}

	public NotFoundException(Throwable cause) {
		super(StatusCode.HTTP_404_NOT_FOUND, cause);
	}

}
