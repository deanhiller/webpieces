package com.webpieces.http2engine.impl.shared.data;

import java.util.concurrent.CompletionException;

public class NoTransitionStreamError extends CompletionException {

	private static final long serialVersionUID = 1L;

	public NoTransitionStreamError() {
	}

	public NoTransitionStreamError(String message) {
		super(message);
	}

	public NoTransitionStreamError(Throwable cause) {
		super(cause);
	}

	public NoTransitionStreamError(String message, Throwable cause) {
		super(message, cause);
	}

}
