package org.webpieces.router.api.exceptions;

import java.util.concurrent.CompletionException;

public class WebpiecesException extends CompletionException {

	private static final long serialVersionUID = 1L;

	public WebpiecesException(String message) {
		super(message);
	}

	public WebpiecesException() {
	}

	public WebpiecesException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebpiecesException(Throwable cause) {
		super(cause);
	}

}
