package org.webpieces.router.api.exceptions;

import com.webpieces.http2parser.api.dto.StatusCode;

public class AuthenticationException extends HttpException {

	private static final long serialVersionUID = 8725117695723001888L;

	public AuthenticationException() {
		super(StatusCode.HTTP_401_UNAUTHORIZED);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(StatusCode.HTTP_401_UNAUTHORIZED, message, cause);
	}

	public AuthenticationException(String message) {
		super(StatusCode.HTTP_401_UNAUTHORIZED, message);
	}

	public AuthenticationException(Throwable cause) {
		super(StatusCode.HTTP_401_UNAUTHORIZED, cause);
	}
}
