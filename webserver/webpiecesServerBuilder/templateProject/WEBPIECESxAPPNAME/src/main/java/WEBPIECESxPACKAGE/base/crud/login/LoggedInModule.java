package WEBPIECESxPACKAGE.base.crud.login;

import static WEBPIECESxPACKAGE.base.crud.login.LoginCrudRouteId.LOGIN_ADD_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.login.LoginCrudRouteId.LOGIN_CONFIRM_DELETE_USER;
import static WEBPIECESxPACKAGE.base.crud.login.LoginCrudRouteId.LOGIN_EDIT_USER_FORM;
import static WEBPIECESxPACKAGE.base.crud.login.LoginCrudRouteId.LOGIN_LIST_USERS;
import static WEBPIECESxPACKAGE.base.crud.login.LoginCrudRouteId.LOGIN_POST_DELETE_USER;
import static WEBPIECESxPACKAGE.base.crud.login.LoginCrudRouteId.LOGIN_POST_USER_FORM;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.CrudRouteIds;
import org.webpieces.router.api.routing.ScopedRouteModule;
import org.webpieces.webserver.api.login.LoginRouteId;

public class LoggedInModule extends ScopedRouteModule {

	@Override
	protected String getScope() {
		return "/secure";
	}

	@Override
	protected void configure() {
		
		addHttpsRoute(HttpMethod.GET ,   "/home",        "CrudController.home", LoginRouteId.LOGGED_IN_HOME);
		
		CrudRouteIds routeIds = new CrudRouteIds(
				LOGIN_LIST_USERS, LOGIN_ADD_USER_FORM, LOGIN_EDIT_USER_FORM,
				LOGIN_POST_USER_FORM, LOGIN_CONFIRM_DELETE_USER, LOGIN_POST_DELETE_USER);
		
		addHttpsCrud("user", "CrudController", routeIds);
	}

}
