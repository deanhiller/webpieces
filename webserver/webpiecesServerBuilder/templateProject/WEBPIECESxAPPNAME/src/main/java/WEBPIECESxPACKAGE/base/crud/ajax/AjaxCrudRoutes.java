package WEBPIECESxPACKAGE.base.crud.ajax;

import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_ADD_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_CONFIRM_DELETE_USER;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_EDIT_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_LIST_USERS;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_POST_DELETE_USER;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_POST_USER_FORM;

import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.ScopedRoutes;

public class AjaxCrudRoutes extends ScopedRoutes {

	@Override
	protected String getScope() {
		return "/secure/ajax";
	}
	
	@Override
	protected void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr) {
		//basic crud example(which just calls the same addRoute methods for you for Create/Read/Update/Delete and 
		//the GET render page views as well)
		//it adds all these routes
		//addRoute(GET ,   "/user/list",        "crud/CrudUserController.userList", listRoute);
		//addRoute(GET ,   "/user/new",         "crud/CrudUserController.userAddEdit", addRoute);
		//addRoute(GET ,   "/user/edit/{id}",   "crud/CrudUserController.userAddEdit", editRoute);
		//addRoute(POST,   "/user/post",        "crud/CrudUserController.postSaveUser", saveRoute);
		//addRoute(GET ,   "/"+entity+"/confirmdelete/{id}", "crud/CrudUserController.confirmDeleteUser", confirmDelete);
		//addRoute(POST,   "/user/delete/{id}", "crud/CrudUserController.postDeleteUser", deleteRoute);
		CrudRouteIds routeIds = new CrudRouteIds(
				AJAX_LIST_USERS, AJAX_ADD_USER_FORM, AJAX_EDIT_USER_FORM,
				AJAX_POST_USER_FORM, AJAX_CONFIRM_DELETE_USER, AJAX_POST_DELETE_USER);
		
		scopedBldr.addCrud(Port.HTTPS, "user", "AjaxCrudUserController", routeIds);
	}

}
