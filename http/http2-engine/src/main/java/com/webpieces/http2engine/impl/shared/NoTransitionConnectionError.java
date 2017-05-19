package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletionException;

public class NoTransitionConnectionError extends CompletionException {

	private static final long serialVersionUID = 1L;

	public NoTransitionConnectionError() {
		// TODO Auto-generated constructor stub
	}

	public NoTransitionConnectionError(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NoTransitionConnectionError(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public NoTransitionConnectionError(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
