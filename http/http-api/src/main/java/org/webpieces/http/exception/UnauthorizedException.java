package org.webpieces.http.exception;

import org.webpieces.http.StatusCode;

public class UnauthorizedException extends HttpClientErrorException {

	private static final long serialVersionUID = 8725117695723001888L;

	public UnauthorizedException() {
		super(StatusCode.HTTP_401_UNAUTHORIZED);
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(StatusCode.HTTP_401_UNAUTHORIZED, message, cause);
	}

	public UnauthorizedException(String message) {
		super(StatusCode.HTTP_401_UNAUTHORIZED, message);
	}

	public UnauthorizedException(Throwable cause) {
		super(StatusCode.HTTP_401_UNAUTHORIZED, cause);
	}

}
