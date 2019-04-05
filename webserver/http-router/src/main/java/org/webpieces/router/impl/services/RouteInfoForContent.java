package org.webpieces.router.impl.services;

import org.webpieces.router.api.extensions.BodyContentBinder;

public class RouteInfoForContent implements RouteData {

	private final BodyContentBinder bodyContentBinder;

	public RouteInfoForContent(BodyContentBinder bodyContentBinder) {
		if(bodyContentBinder == null)
			throw new IllegalArgumentException("bodyContentBinder is required for these routes yet it is null here.  bug");
		this.bodyContentBinder = bodyContentBinder;
	}

	public BodyContentBinder getBodyContentBinder() {
		return bodyContentBinder;
	}

}
