package org.webpieces.router.impl.loader.svc;

import org.webpieces.router.api.extensions.BodyContentBinder;

public class RouteInfoForContent implements RouteInfo {

	private final BodyContentBinder bodyContentBinder;

	public RouteInfoForContent(BodyContentBinder bodyContentBinder) {
		this.bodyContentBinder = bodyContentBinder;
	}

	public BodyContentBinder getBodyContentBinder() {
		return bodyContentBinder;
	}

}
