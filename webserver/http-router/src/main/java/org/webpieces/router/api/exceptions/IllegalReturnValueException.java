package org.webpieces.router.api.exceptions;

import java.util.concurrent.CompletionException;

public class IllegalReturnValueException extends CompletionException {

	private static final long serialVersionUID = -2996758528390443016L;

	public IllegalReturnValueException() {
		super();
	}

	public IllegalReturnValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalReturnValueException(String message) {
		super(message);
	}

	public IllegalReturnValueException(Throwable cause) {
		super(cause);
	}
	
}
