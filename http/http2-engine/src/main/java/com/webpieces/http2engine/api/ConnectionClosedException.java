package com.webpieces.http2engine.api;

import com.webpieces.http2engine.api.error.ConnectionCancelled;

public class ConnectionClosedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConnectionClosedException(ConnectionCancelled closedReason, String message) {
		super(message);
	}

}
