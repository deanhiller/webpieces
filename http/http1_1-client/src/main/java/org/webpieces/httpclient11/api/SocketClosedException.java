package org.webpieces.httpclient11.api;

import java.util.concurrent.CompletionException;

public class SocketClosedException extends CompletionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6637694654798406268L;

	public SocketClosedException() {
		super();
	}

	public SocketClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SocketClosedException(String message) {
		super(message);
	}

	public SocketClosedException(Throwable cause) {
		super(cause);
	}

}
