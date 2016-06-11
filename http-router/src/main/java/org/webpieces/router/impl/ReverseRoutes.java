package org.webpieces.router.impl;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.router.api.routing.RouteId;

public class ReverseRoutes {

	private Map<RouteId, RouteMeta> routeIdToRoute = new HashMap<>();
	
	public void addRoute(RouteId routeId, RouteMeta meta) {
		RouteMeta existingRoute = routeIdToRoute.get(routeId);
		if(existingRoute != null) {
			throw new IllegalStateException("You cannot use a RouteId twice.  routeId="+routeId
					+" first time="+existingRoute.getRoute().getPath()+" second time="+meta.getRoute().getPath());
		}

		routeIdToRoute.put(routeId, meta);
	}

	public RouteMeta get(RouteId id) {
		return routeIdToRoute.get(id);
	}

}
