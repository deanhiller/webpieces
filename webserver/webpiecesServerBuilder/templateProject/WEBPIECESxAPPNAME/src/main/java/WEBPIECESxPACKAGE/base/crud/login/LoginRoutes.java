package WEBPIECESxPACKAGE.base.crud.login;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;
import org.webpieces.webserver.api.login.AbstractLoginRoutes;

/** 
 * Move this to the client applications instead since it is specific to one of the app's login methods
 * @author dhiller
 *
 */
public class LoginRoutes extends AbstractLoginRoutes {

	/**
	 * @param controller
	 * @param basePath The 'unsecure' path that has a login page so you can get to the secure path
	 * @param securePath The path for the secure filter that ensures everyone under that path is secure
	 * @param sessionToken
	 */
	public LoginRoutes(String controller, String securePath) {
		super(controller, null, securePath);
	}
	
	@Override
	protected RouteId getPostLoginRoute() {
		return LoginRouteId.POST_LOGIN;
	}

	@Override
	protected RouteId getRenderLoginRoute() {
		return LoginRouteId.LOGIN;
	}

	@Override
	protected RouteId getRenderLogoutRoute() {
		return LoginRouteId.LOGOUT;
	}

	@Override
	protected String getSessionToken() {
		return AppLoginController.TOKEN;
	}

	@Override
	protected void addLoggedInHome(Router httpsRouter) {
		Router scopedRouter = httpsRouter.getScopedRouter("/secure", true);
		scopedRouter.addRoute(HttpMethod.GET ,   "/loggedinhome",        "AppLoginController.home", LoginRouteId.LOGGED_IN_HOME);
	}

}
