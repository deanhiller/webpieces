package org.webpieces.httpclient.api.exceptions;

public class ResetStreamException extends RuntimeException {

	private static final long serialVersionUID = 6885706690628665943L;

	public ResetStreamException() {
		super();
	}

	public ResetStreamException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ResetStreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResetStreamException(String message) {
		super(message);
	}

	public ResetStreamException(Throwable cause) {
		super(cause);
	}
	
}
