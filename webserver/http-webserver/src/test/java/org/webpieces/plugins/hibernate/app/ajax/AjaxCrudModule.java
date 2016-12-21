package org.webpieces.plugins.hibernate.app.ajax;

import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.*;

import org.webpieces.router.api.routing.CrudRouteIds;
import org.webpieces.router.api.routing.ScopedRouteModule;

public class AjaxCrudModule extends ScopedRouteModule {

	@Override
	protected String getScope() {
		return "/ajax";
	}

	@Override
	protected void configure() {
		CrudRouteIds routeIds = new CrudRouteIds(
				AJAX_LIST_USERS, AJAX_ADD_USER_FORM, AJAX_EDIT_USER_FORM,
				AJAX_POST_USER_FORM, AJAX_CONFIRM_DELETE_USER, AJAX_POST_DELETE_USER);
		
		addCrud("user", "AjaxCrudTestController", routeIds);
	}

}
