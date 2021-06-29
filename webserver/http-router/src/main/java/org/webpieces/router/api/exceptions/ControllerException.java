package org.webpieces.router.api.exceptions;

import org.webpieces.util.exceptions.WebpiecesException;

public class ControllerException extends WebpiecesException {

	private static final long serialVersionUID = 1L;

	public ControllerException(String msg, Throwable t) {
		super(msg, t);
	}
}
