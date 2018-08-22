package org.webpieces.router.api.routing;

public interface ReverseRouteLookup {

	boolean isGetRequest(RouteId routeId);

	//TODO: add a parameterized one(very easy to do)
	String convertToUrl(RouteId routeId);

}
