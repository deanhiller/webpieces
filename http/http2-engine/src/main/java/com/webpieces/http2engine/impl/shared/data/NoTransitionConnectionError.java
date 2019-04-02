package com.webpieces.http2engine.impl.shared.data;

import java.util.concurrent.CompletionException;

public class NoTransitionConnectionError extends CompletionException {

	private static final long serialVersionUID = 1L;

	public NoTransitionConnectionError() {
	}

	public NoTransitionConnectionError(String message) {
		super(message);
	}

	public NoTransitionConnectionError(Throwable cause) {
		super(cause);
	}

	public NoTransitionConnectionError(String message, Throwable cause) {
		super(message, cause);
	}

}
