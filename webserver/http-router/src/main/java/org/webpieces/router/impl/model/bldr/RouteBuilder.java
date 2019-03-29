package org.webpieces.router.impl.model.bldr;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteFilter;

public interface RouteBuilder extends ScopedRouteBuilder {
	
	<T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type);
	
	<T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type);

	<T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type);
	
	/**
	 * This is the controller for 404's where the path was not found AND this MUST be set
	 */
	void setPageNotFoundRoute(String controllerMethod);
	
	void setInternalErrorRoute(String controllerMethod);

}
