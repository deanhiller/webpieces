package webpiecesxxxxxpackage.web.login;

import static org.webpieces.router.api.routes.Port.HTTPS;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.RouteId;
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
	public LoginRoutes(String controller, String securePath, String ... secureFields) {
		//filter apply level is made to be high as we want to run this filter above most since it's fast and needs no DB connection
		super(controller, null, securePath, 10000, secureFields);
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
	protected void addLoggedInHome(RouteBuilder baseRouter, ScopedRouteBuilder scopedRouter1) {
		ScopedRouteBuilder scopedRouter = scopedRouter1.getScopedRouteBuilder("/secure");
		scopedRouter.addRoute(HTTPS, HttpMethod.GET ,   "/loggedinhome",        "AppLoginController.loginHome", LoginRouteId.LOGGED_IN_HOME);
	}

	@Override
	protected RouteBuilder fetchBuilder(DomainRouteBuilder domainRouteBldr) {
		return domainRouteBldr.getAllDomainsRouteBuilder();
	}

}
