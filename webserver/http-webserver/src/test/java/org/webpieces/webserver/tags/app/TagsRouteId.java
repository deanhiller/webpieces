package org.webpieces.webserver.tags.app;

import org.webpieces.router.api.routing.RouteId;

public enum TagsRouteId implements RouteId {
	VERBATIM_ROUTE_ID, IF_ROUTE_ID, ELSE_ROUTE_ID, ELSEIF, SETGET, EXTENDS, AHREF, CUSTOM_TAG, RENDER_TAG_ARGS_TAG, RENDER_PAGE_ARGS_TAG, FIELD_TAG, GET_USER_FORM, POST_USER
	
	//This is where you define the ids of routes that you can use in the controllers to redirect to a route
	//or use in the webpages
}