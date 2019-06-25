package org.webpieces.router.api.exceptions;

public abstract class HttpException extends WebpiecesException {
	private static final long serialVersionUID = -4338007033423601133L;

	public HttpException() {
		super();
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(Throwable cause) {
        super(cause);
    }
}
