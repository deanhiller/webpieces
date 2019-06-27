package org.webpieces.webserver.impl.filereaders;

import java.util.concurrent.CompletionException;

public class ReadOrSendException extends CompletionException {

	private static final long serialVersionUID = 1L;

	public ReadOrSendException() {
	}

	public ReadOrSendException(String message) {
		super(message);
	}

	public ReadOrSendException(Throwable cause) {
		super(cause);
	}

	public ReadOrSendException(String message, Throwable cause) {
		super(message, cause);
	}

}
