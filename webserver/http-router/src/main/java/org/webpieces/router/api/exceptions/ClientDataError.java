package org.webpieces.router.api.exceptions;

public class ClientDataError extends WebpiecesException {

	private static final long serialVersionUID = 8725117695723001888L;

	public ClientDataError() {
		super();
	}

	public ClientDataError(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientDataError(String message) {
		super(message);
	}

	public ClientDataError(Throwable cause) {
		super(cause);
	}
}
