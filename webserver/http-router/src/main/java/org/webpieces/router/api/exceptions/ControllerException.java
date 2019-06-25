package org.webpieces.router.api.exceptions;

public class ControllerException extends WebpiecesException {

	private static final long serialVersionUID = 1L;

	public ControllerException(String msg, Throwable t) {
		super(msg, t);
	}
}
