package org.webpieces.router.api.routing;

import org.webpieces.router.impl.RouteMeta;

public interface ReverseRouteLookup {

	RouteMeta get(RouteId routeId);

}
