package org.webpieces.router.api.exceptions;

import org.webpieces.util.exceptions.WebpiecesException;

public class IllegalArgException extends WebpiecesException {

	private static final long serialVersionUID = 1L;

	public IllegalArgException(String message) {
		super(message);
	}

	public IllegalArgException() {
	}

	public IllegalArgException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalArgException(Throwable cause) {
		super(cause);
	}

}
