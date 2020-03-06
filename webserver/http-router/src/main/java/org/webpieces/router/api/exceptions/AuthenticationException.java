package org.webpieces.router.api.exceptions;

public class AuthenticationException extends WebpiecesException {

	private static final long serialVersionUID = 8725117695723001888L;

	public AuthenticationException() {
		super();
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}
}
