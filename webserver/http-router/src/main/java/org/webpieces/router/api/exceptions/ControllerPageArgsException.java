package org.webpieces.router.api.exceptions;

public class ControllerPageArgsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ControllerPageArgsException() {
		super();
	}

	public ControllerPageArgsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ControllerPageArgsException(String message, Throwable cause) {
		super(message, cause);
	}

	public ControllerPageArgsException(String message) {
		super(message);
	}

	public ControllerPageArgsException(Throwable cause) {
		super(cause);
	}
	
}
