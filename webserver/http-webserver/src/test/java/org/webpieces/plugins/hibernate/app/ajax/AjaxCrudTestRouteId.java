package org.webpieces.plugins.hibernate.app.ajax;

import org.webpieces.router.api.routes.RouteId;

public enum AjaxCrudTestRouteId  implements RouteId {
	AJAX_LIST_USERS, 
	AJAX_ADD_USER_FORM, AJAX_EDIT_USER_FORM, AJAX_POST_USER_FORM, 
	AJAX_CONFIRM_DELETE_USER, AJAX_POST_DELETE_USER

}
