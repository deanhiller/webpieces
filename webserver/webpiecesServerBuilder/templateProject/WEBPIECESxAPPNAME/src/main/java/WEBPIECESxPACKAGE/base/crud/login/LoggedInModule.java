package WEBPIECESxPACKAGE.base.crud.login;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.ScopedRouteModule;
import org.webpieces.webserver.api.login.LoginRouteId;

public class LoggedInModule extends ScopedRouteModule {

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
