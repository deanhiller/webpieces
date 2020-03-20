package org.webpieces.plugins.hibernate.app.ajax;

import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.ScopedRoutes;

import static org.webpieces.plugins.hibernate.app.ajax.AjaxCrudTestRouteId.*;

public class AjaxHibernateCrudRoutes extends ScopedRoutes {

	@Override
	protected String getScope() {
		return "/ajax";
	}

	@Override
	protected void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr) {
		CrudRouteIds routeIds = new CrudRouteIds(
				AJAX_LIST_USERS, AJAX_ADD_USER_FORM, AJAX_EDIT_USER_FORM,
				AJAX_POST_USER_FORM, AJAX_CONFIRM_DELETE_USER, AJAX_POST_DELETE_USER);
		
		scopedBldr.addCrud(Port.BOTH, "user", "AjaxCrudTestController", routeIds);
	}

}
