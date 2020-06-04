package com.webpieces.http2engine.api.error;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;

public class ConnectionFailure implements ShutdownConnection {

	private ConnectionException cause;

	public ConnectionFailure(ConnectionException cause) {
		super();
		this.cause = cause;
	}

	@Override
	public String getReason() {
		return cause.getClass().getSimpleName()+": "+cause.getMessage();
	}

	@Override
	public CancelReasonCode getReasonCode() {
		return cause.getReason();
	}

	public ConnectionException getCause() {
		return cause;
	}

	@Override
	public String toString() {
		return "ConnectionFailure [cause=" + cause.getMessage() + "]";
	}

}
