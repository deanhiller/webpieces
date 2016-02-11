package org.playorm.nio.api.handlers;

public class NioInterruptException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public NioInterruptException() {
		super();
	}

	public NioInterruptException(String message, Throwable cause) {
		super(message, cause);
	}

	public NioInterruptException(String message) {
		super(message);
	}

	public NioInterruptException(Throwable cause) {
		super(cause);
	}
}
