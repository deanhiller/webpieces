package org.webpieces.webserver.impl.filereaders;

import java.util.concurrent.CompletionException;

public class XFileReadException extends CompletionException {

	private static final long serialVersionUID = 1L;

	public XFileReadException() {
		super();
	}

	public XFileReadException(String message, Throwable cause) {
		super(message, cause);
	}

	public XFileReadException(String message) {
		super(message);
	}

	public XFileReadException(Throwable cause) {
		super(cause);
	}

	
}
