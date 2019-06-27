package org.webpieces.router.api.exceptions;

import java.util.concurrent.CompletionException;

public class WebSocketClosedException extends CompletionException {

	private static final long serialVersionUID = 1L;

	public WebSocketClosedException() {
		super();
	}

	public WebSocketClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebSocketClosedException(String message) {
		super(message);
	}

	public WebSocketClosedException(Throwable cause) {
		super(cause);
	}

}
