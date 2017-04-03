package WEBPIECESxPACKAGE.base.crud.login;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.ScopedRouteModule;
import org.webpieces.webserver.api.login.LoginRouteId;

import WEBPIECESxPACKAGE.base.examples.ExamplesRouteId;

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

		addRoute(HttpMethod.GET ,   "/loginhome",        "AppLoginController.home", LoginRouteId.LOGGED_IN_HOME);
		
	}

}
