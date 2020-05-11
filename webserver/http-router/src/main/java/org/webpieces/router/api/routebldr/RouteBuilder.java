package org.webpieces.router.api.routebldr;

import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.RouteFilter;

public interface RouteBuilder extends ScopedRouteBuilder {

	/**
	 * Adds a filter to controllers whose 'route' matches the pathRegEx.  NOTE: This pathRegEx DOES NOT apply to the incoming request to enhance
	 * performance BUT ONLY applies to the controller's route path of "/mypath/{username}".  In doing so, all filters are chained on startup so
	 * when requests come in, it's very fast.
	 * 
	 * This only adds request/response type route filters.  addStreamRoute would need streaming filters.
	 */
	<T> void addFilter(String pathRegEx, Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel);

	/**
	 * Adds a filter on any Controllers whose getClass().getName() matches this regEx.  This is made so you can have a package
	 * com.mycompany.web and apply a filter to all controllers in that package AND THEN have a package com.mycompany.web.secure and apply
	 * a login filter to that package.  You could also do something like a regexc that matches all controllers starting with the name Secure.
	 * 
	 * All filters are applied on startup so that performance is very fast at handling requests.
	 */
	<T> void addPackageFilter(String regEx, Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel);

	/**
	 * When a controller is not found to call.  These are all the filters that are applied to the not found controller below.
	 */
	<T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel);

	/**
	 * When there is an error, the internal error controller is called.  These are all the filters that are applied to the internal error
	 * controller method
	 */
	<T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel);
	
	/**
	 * This is the controller for 404's where the path was not found AND this MUST be set
	 */
	void setPageNotFoundRoute(String controllerMethod);
	
	void setInternalErrorRoute(String controllerMethod);
	
}
