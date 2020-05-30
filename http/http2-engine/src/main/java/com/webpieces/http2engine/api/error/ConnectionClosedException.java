package com.webpieces.http2engine.api.error;

public class ConnectionClosedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConnectionClosedException(ConnectionCancelled closedReason, String message) {
		super(message);
	}

}
