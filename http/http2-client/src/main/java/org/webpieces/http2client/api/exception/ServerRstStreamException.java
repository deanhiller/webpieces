package org.webpieces.http2client.api.exception;

public class ServerRstStreamException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServerRstStreamException(String msg) {
		super(msg);
	}

	
}
