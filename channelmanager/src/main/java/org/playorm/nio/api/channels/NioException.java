package org.playorm.nio.api.channels;

public class NioException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NioException() {
	}

	public NioException(String message) {
		super(message);
	}

	public NioException(Throwable cause) {
		super(cause);
	}

	public NioException(String message, Throwable cause) {
		super(message, cause);
	}

}
