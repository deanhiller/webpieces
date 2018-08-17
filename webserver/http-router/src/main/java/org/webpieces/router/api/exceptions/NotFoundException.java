package org.webpieces.router.api.exceptions;

public class NotFoundException extends HttpException {

	private static final long serialVersionUID = 7804145831639203745L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}

}
