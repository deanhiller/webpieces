package org.webpieces.compiler.api;

public class ClassFileNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClassFileNotFoundException() {
		super();
	}

	public ClassFileNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ClassFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClassFileNotFoundException(String message) {
		super(message);
	}

	public ClassFileNotFoundException(Throwable cause) {
		super(cause);
	}	
	

}
