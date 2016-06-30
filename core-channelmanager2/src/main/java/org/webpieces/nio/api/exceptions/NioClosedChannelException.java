package org.webpieces.nio.api.exceptions;

public class NioClosedChannelException extends NioException {

	private static final long serialVersionUID = 8487660677588451967L;

	public NioClosedChannelException() {
		super();
	}

	public NioClosedChannelException(String message, Throwable cause) {
		super(message, cause);
	}

	public NioClosedChannelException(String message) {
		super(message);
	}

	public NioClosedChannelException(Throwable cause) {
		super(cause);
	}

}
