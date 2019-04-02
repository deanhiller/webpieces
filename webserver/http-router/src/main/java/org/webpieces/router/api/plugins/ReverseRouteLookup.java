package org.webpieces.router.api.plugins;

import org.webpieces.router.api.routes.RouteId;

public interface ReverseRouteLookup {

	boolean isGetRequest(RouteId routeId);

	String convertToUrl(RouteId routeId);

}
