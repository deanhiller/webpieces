package WEBPIECESxPACKAGE.base;

import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_ADD_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_EDIT_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_LIST_USERS;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_CONFIRM_DELETE_USER;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_POST_DELETE_USER;
import static WEBPIECESxPACKAGE.base.crud.ajax.AjaxCrudUserRouteId.AJAX_POST_USER_FORM;

import org.webpieces.router.api.routing.CrudRouteIds;
import org.webpieces.router.api.routing.ScopedRouteModule;

public class AjaxCrudRouteModule extends ScopedRouteModule {

	@Override
	protected String getScope() {
		return "/ajax";
	}

	@Override
	protected void configure() {
		//basic crud example(which just calls the same addRoute methods for you for Create/Read/Update/Delete and 
		//the GET render page views as well)
		//it adds all these routes
		//addRoute(GET ,   "/user/list",        "crud/CrudUserController.userList", listRoute);
		//addRoute(GET ,   "/user/new",         "crud/CrudUserController.userAddEdit", addRoute);
		//addRoute(GET ,   "/user/edit/{id}",   "crud/CrudUserController.userAddEdit", editRoute);
		//addRoute(POST,   "/user/post",        "crud/CrudUserController.postSaveUser", saveRoute);
		//addRoute(POST,   "/user/delete/{id}", "crud/CrudUserController.postDeleteUser", deleteRoute);
		//Not sure on this next one yet as we re-use the post route(it's easier for the webapp developer that way)
		//XXXX(maybe not this one) addRoute(PUT, "/user/post/{id}",      "crud/CrudUserController.postSaveUser", saveRoute);
		CrudRouteIds routeIds = new CrudRouteIds(
				AJAX_LIST_USERS, AJAX_ADD_USER_FORM, AJAX_EDIT_USER_FORM,
				AJAX_POST_USER_FORM, AJAX_CONFIRM_DELETE_USER, AJAX_POST_DELETE_USER);
		
		addCrud("user", "crud/ajax/AjaxCrudUserController", routeIds);
	}

}
