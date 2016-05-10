package org.webpieces.ssl.api;

public class AsyncSSLEngineException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AsyncSSLEngineException() {
	}

	public AsyncSSLEngineException(String message) {
		super(message);
	}

	public AsyncSSLEngineException(Throwable cause) {
		super(cause);
	}

	public AsyncSSLEngineException(String message, Throwable cause) {
		super(message, cause);
	}

}
