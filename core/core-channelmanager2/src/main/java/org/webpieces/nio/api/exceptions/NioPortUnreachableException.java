package org.webpieces.nio.api.exceptions;

public class NioPortUnreachableException extends NioException {

	private static final long serialVersionUID = 8487660677588451967L;

	public NioPortUnreachableException() {
		super();
	}

	public NioPortUnreachableException(String message, Throwable cause) {
		super(message, cause);
	}

	public NioPortUnreachableException(String message) {
		super(message);
	}

	public NioPortUnreachableException(Throwable cause) {
		super(cause);
	}

}
