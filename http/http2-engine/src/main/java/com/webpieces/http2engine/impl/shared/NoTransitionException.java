package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletionException;

public class NoTransitionException extends CompletionException {

	private static final long serialVersionUID = 1L;

	public NoTransitionException() {
		// TODO Auto-generated constructor stub
	}

	public NoTransitionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NoTransitionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public NoTransitionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
