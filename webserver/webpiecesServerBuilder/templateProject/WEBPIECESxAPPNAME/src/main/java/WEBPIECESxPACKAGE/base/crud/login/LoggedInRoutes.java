package WEBPIECESxPACKAGE.base.crud.login;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.ScopedRoutes;
import org.webpieces.webserver.api.login.LoginRouteId;

public class LoggedInRoutes extends ScopedRoutes {

	@Override
	protected String getScope() {
		return "/secure";
	}

	@Override
	protected boolean isHttpsOnlyRoutes() {
		return true;
	}
	
	@Override
	protected void configure() {

		addRoute(HttpMethod.GET ,   "/loggedinhome",        "AppLoginController.home", LoginRouteId.LOGGED_IN_HOME);
		
	}

}
