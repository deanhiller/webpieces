package org.webpieces.router.api.exceptions;

public class AuthorizationException extends WebpiecesException {

	private static final long serialVersionUID = 8725117695723001888L;

	public AuthorizationException() {
		super();
	}

	public AuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizationException(String message) {
		super(message);
	}

	public AuthorizationException(Throwable cause) {
		super(cause);
	}
}
