package org.webpieces.plugins.hibernate.app;

import org.webpieces.router.api.routing.RouteId;

public enum HibernateRouteId implements RouteId {
	SAVE_ENTITY, DISPLAY_ENTITY, ASYNC_SAVE_ENTITY, ASYNC_DISPLAY_ENTITY, ENTITY_LOAD, ASYNC_ENTITY_LOAD, 
	
	LIST_USERS, ADD_USER_PAGE, EDIT_USER_PAGE, SAVE_USER, 
	CONFIRM_DELETE_USER, DELETE_USER, 
	
	MERGE_ENTITY
	
}