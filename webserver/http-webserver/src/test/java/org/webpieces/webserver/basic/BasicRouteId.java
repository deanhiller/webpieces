package org.webpieces.webserver.basic;

import org.webpieces.router.api.routing.RouteId;

public enum BasicRouteId implements RouteId {
	
	//This is where you define the ids of routes that you can use in the controllers to redirect to a route
	//or use in the webpages
	REDIRECT_PAGE, RENDER_PAGE, SOME_ROUTE, REDIRECT2, THROW_NOT_FOUND, 
	PAGE_PARAM, VERBATIM, IF, ELSE, ELSEIF, SETGET, EXTENDS, AHREF, URLENCODE
}