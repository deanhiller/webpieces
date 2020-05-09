package org.webpieces.nio.api.exceptions;

import org.webpieces.util.exceptions.NioException;

public class NioTimeoutException extends NioException {

	private static final long serialVersionUID = 8487660677588451967L;

	public NioTimeoutException() {
		super();
	}

	public NioTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public NioTimeoutException(String message) {
		super(message);
	}

	public NioTimeoutException(Throwable cause) {
		super(cause);
	}

}
