package org.webpieces.plugins.hibernate.app.ajax;

import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.AJAX_ADD_USER_FORM;
import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.AJAX_CONFIRM_DELETE_USER;
import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.AJAX_EDIT_USER_FORM;
import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.AJAX_LIST_USERS;
import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.AJAX_POST_DELETE_USER;
import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.AJAX_POST_USER_FORM;

import org.webpieces.router.api.routing.CrudRouteIds;
import org.webpieces.router.api.routing.Port;
import org.webpieces.router.api.routing.ScopedRoutes;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.ScopedRouteBuilder;

public class AjaxHibernateCrudRoutes extends ScopedRoutes {

	@Override
	protected String getScope() {
		return "/ajax";
	}

	@Override
	protected void configure(RouteBuilder baseRouter, ScopedRouteBuilder scopedRouter) {
		CrudRouteIds routeIds = new CrudRouteIds(
				AJAX_LIST_USERS, AJAX_ADD_USER_FORM, AJAX_EDIT_USER_FORM,
				AJAX_POST_USER_FORM, AJAX_CONFIRM_DELETE_USER, AJAX_POST_DELETE_USER);
		
		scopedRouter.addCrud(Port.BOTH, "user", "AjaxCrudTestController", routeIds);
	}

}
