package org.webpieces.router.api.routebldr;

import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.RouteFilter;

public interface RouteBuilder extends ScopedRouteBuilder {
	
	<T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type);
	
	<T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type);

	<T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type);
	
	/**
	 * This is the controller for 404's where the path was not found AND this MUST be set
	 */
	void setPageNotFoundRoute(String controllerMethod);
	
	void setInternalErrorRoute(String controllerMethod);

}
