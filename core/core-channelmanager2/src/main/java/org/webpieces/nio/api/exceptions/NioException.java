package org.webpieces.nio.api.exceptions;

import java.util.concurrent.CompletionException;

public class NioException extends CompletionException {

	private static final long serialVersionUID = 1L;

	public NioException() {
	}

	public NioException(String message) {
		super(message);
	}

	public NioException(Throwable cause) {
		super(cause);
	}

	public NioException(String message, Throwable cause) {
		super(message, cause);
	}

}
