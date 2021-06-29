package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class ForbiddenException extends HttpClientErrorException {

	private static final long serialVersionUID = 8725117695723001888L;

	public ForbiddenException() {
		super(StatusCode.HTTP_403_FORBIDDEN);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(StatusCode.HTTP_403_FORBIDDEN, message, cause);
	}

	public ForbiddenException(String message) {
		super(StatusCode.HTTP_403_FORBIDDEN, message);
	}

	public ForbiddenException(Throwable cause) {
		super(StatusCode.HTTP_403_FORBIDDEN, cause);
	}

}
