package org.webpieces.webserver.basic.app;

import org.webpieces.router.api.routing.RouteId;

public enum BasicRouteId implements RouteId {
	
	//This is where you define the ids of routes that you can use in the controllers to redirect to a route
	//or use in the webpages
	REDIRECT_PAGE, RENDER_PAGE, SOME_ROUTE, REDIRECT2, THROW_NOT_FOUND, BAD_TEMPLATE, JSON_ROUTE, NULL_ROUTE, REDIRECT_RAW_URL, REDIRECT_ABSOLUTE_URL 
	
}