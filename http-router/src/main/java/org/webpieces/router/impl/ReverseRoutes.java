package org.webpieces.router.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.api.Route;
import org.webpieces.router.api.RouteId;

public class ReverseRoutes {

	private Map<RouteId, Route> routeIdToRoute = new HashMap<>();
	
	public void addRoute(RouteId routeId, Route route) {
		Route existingRoute = routeIdToRoute.get(routeId);
		if(existingRoute != null)
			throw new IllegalStateException("You cannot use a RouteId twice.  routeId="+routeId+" first time="+existingRoute.getPath()+" second time="+route.getPath());

		routeIdToRoute.put(routeId, route);
	}

	public Collection<Route> getAllRoutes() {
		return routeIdToRoute.values();
	}

}
