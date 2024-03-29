package org.webpieces.router.impl.services;

import org.webpieces.http.exception.NotFoundException;

public class RouteInfoForNotFound implements RouteData {

	private final NotFoundException notFoundException;

	public NotFoundException getNotFoundException() {
		return notFoundException;
	}

	public RouteInfoForNotFound(NotFoundException notFoundException) {
		this.notFoundException = notFoundException;
	}

}
