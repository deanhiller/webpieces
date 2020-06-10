package org.webpieces.router.impl.services;

public class RouteInfoForInternalError implements RouteData {


    private Throwable exception;

	public RouteInfoForInternalError(Throwable exception) {
		this.exception = exception;
    }

	public Throwable getException() {
		return exception;
	}

}
