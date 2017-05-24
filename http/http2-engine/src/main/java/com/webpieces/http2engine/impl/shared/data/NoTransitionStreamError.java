package com.webpieces.http2engine.impl.shared.data;

import java.util.concurrent.CompletionException;

public class NoTransitionStreamError extends CompletionException {

	private static final long serialVersionUID = 1L;

	public NoTransitionStreamError() {
		// TODO Auto-generated constructor stub
	}

	public NoTransitionStreamError(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NoTransitionStreamError(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public NoTransitionStreamError(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
