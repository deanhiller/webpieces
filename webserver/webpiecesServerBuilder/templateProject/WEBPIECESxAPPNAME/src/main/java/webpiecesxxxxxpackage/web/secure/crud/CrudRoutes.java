package webpiecesxxxxxpackage.web.secure.crud;

import static webpiecesxxxxxpackage.web.secure.crud.CrudUserRouteId.CONFIRM_DELETE_USER;
import static webpiecesxxxxxpackage.web.secure.crud.CrudUserRouteId.GET_ADD_USER_FORM;
import static webpiecesxxxxxpackage.web.secure.crud.CrudUserRouteId.GET_EDIT_USER_FORM;
import static webpiecesxxxxxpackage.web.secure.crud.CrudUserRouteId.LIST_USERS;
import static webpiecesxxxxxpackage.web.secure.crud.CrudUserRouteId.POST_DELETE_USER;
import static webpiecesxxxxxpackage.web.secure.crud.CrudUserRouteId.POST_USER_FORM;

import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.ScopedRoutes;

public class CrudRoutes extends ScopedRoutes {

	@Override
	protected String getScope() {
		return "/crud";
	}
	
	@Override
	protected void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr) {
		//basic crud example(which just calls the same addRoute methods for you for Create/Read/Update/Delete and 
		//the GET render page views as well)
		//it adds all these routes for you in one method call
		//addRoute(GET ,   "/user/list",        "crud/CrudUserController.userList", listRoute);
		//addRoute(GET ,   "/user/new",         "crud/CrudUserController.userAddEdit", addRoute);
		//addRoute(GET ,   "/user/edit/{id}",   "crud/CrudUserController.userAddEdit", editRoute);
		//addRoute(POST,   "/user/post",        "crud/CrudUserController.postSaveUser", saveRoute);
		//addRoute(GET,    "/user/confirmdelete/{id}", "crud/CrudUserController.confirmDeleteUser", confirmDelete);
		//addRoute(POST,   "/user/delete/{id}", "crud/CrudUserController.postDeleteUser", deleteRoute);

		CrudRouteIds routeIds = new CrudRouteIds(
				LIST_USERS, GET_ADD_USER_FORM, GET_EDIT_USER_FORM,
				POST_USER_FORM, CONFIRM_DELETE_USER, POST_DELETE_USER);
		scopedBldr.addCrud(Port.HTTPS, "user", "CrudUserController", routeIds);
	}

}
