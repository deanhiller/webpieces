package org.playorm.nio.api.libs;

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
