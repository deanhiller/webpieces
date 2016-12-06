package org.webpieces.plugins.hibernate.app;

import org.webpieces.router.api.routing.RouteId;

public enum HibernateRouteId implements RouteId {
	SAVE_ENTITY, DISPLAY_ENTITY, ASYNC_SAVE_ENTITY, ASYNC_DISPLAY_ENTITY, ENTITY_LOAD, ASYNC_ENTITY_LOAD, 
	
	LIST_USERS, ADD_EDIT_USER_PAGE, SAVE_USER, DELETE_USER
	
}