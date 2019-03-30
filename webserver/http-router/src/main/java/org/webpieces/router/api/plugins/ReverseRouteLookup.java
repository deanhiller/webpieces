package org.webpieces.router.api.plugins;

import org.webpieces.router.api.routes.RouteId;

public interface ReverseRouteLookup {

	boolean isGetRequest(RouteId routeId);

	//TODO: add a parameterized one(very easy to do)
	String convertToUrl(RouteId routeId);

}
