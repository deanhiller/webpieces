package org.webpieces.router.impl.model.bldr.data;

import java.util.concurrent.CompletionException;

public class InternalErrorRouteFailedException extends CompletionException {

	private static final long serialVersionUID = 1L;
	private Object failedRoute;

	public InternalErrorRouteFailedException(Throwable t, Object failedRoute) {
		super();
		this.failedRoute = failedRoute;
	}

	public InternalErrorRouteFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public InternalErrorRouteFailedException(String message) {
		super(message);
	}

	public InternalErrorRouteFailedException(Throwable cause) {
		super(cause);
	}
}
