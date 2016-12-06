package org.webpieces.jdbc.api;

public class SqlRuntimeException extends RuntimeException {

	public SqlRuntimeException() {
		super();
	}

	public SqlRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SqlRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlRuntimeException(String message) {
		super(message);
	}

	public SqlRuntimeException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}
