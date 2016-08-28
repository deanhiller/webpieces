package org.webpieces.webserver.async;

import org.webpieces.router.api.routing.RouteId;

public enum AsyncRouteId implements RouteId {
	
	//This is where you define the ids of routes that you can use in the controllers to redirect to a route
	//or use in the webpages
	REDIRECT_PAGE, RENDER_PAGE, SOME_ROUTE, REDIRECT2, THROW_NOT_FOUND, ASYNC_SUCCESS, ASYNC_FAIL
}