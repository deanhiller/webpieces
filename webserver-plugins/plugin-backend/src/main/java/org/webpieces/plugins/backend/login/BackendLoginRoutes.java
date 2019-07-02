package org.webpieces.plugins.backend.login;

import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.webserver.api.login.AbstractLoginRoutes;

public class BackendLoginRoutes extends AbstractLoginRoutes {

	private boolean usePluginAssets;

	/**
	 * @param controller
	 * @param basePath The 'unsecure' path that has a login page so you can get to the secure path
	 * @param securePath The path for the secure filter that ensures everyone under that path is secure
	 */
	public BackendLoginRoutes(boolean usePluginAssets, String controller, String basePath, String securePath) {
		super(controller, basePath, securePath, "password");
		this.usePluginAssets = usePluginAssets;
	}
	
	@Override
	protected RouteId getPostLoginRoute() {
		return BackendLoginRouteId.POST_BACKEND_LOGIN;
	}

	@Override
	protected RouteId getRenderLoginRoute() {
		return BackendLoginRouteId.BACKEND_LOGIN;
	}

	@Override
	protected RouteId getRenderLogoutRoute() {
		return BackendLoginRouteId.BACKEND_LOGOUT;
	}

	@Override
	protected String getSessionToken() {
		return BackendLoginController.TOKEN;
	}

	@Override
	protected void addLoggedInHome(RouteBuilder baseRouter, ScopedRouteBuilder scope1Router) {
		ScopedRouteBuilder scopedRouter = scope1Router.getScopedRouteBuilder("/secure");
		scopedRouter.addRoute(Port.HTTPS, HttpMethod.GET ,   "/loggedinhome",        "org.webpieces.plugins.backend.BackendController.home", BackendLoginRouteId.BACKEND_LOGGED_IN_HOME);
		
		ScopedRouteBuilder scoped2 = baseRouter.getScopedRouteBuilder(basePath);
		scoped2.addRoute(Port.BOTH, HttpMethod.GET,  "", "org.webpieces.plugins.backend.BackendController.redirectToLogin", BackendLoginRouteId.LOGGED_OUT_LANDING);
		
		if(usePluginAssets) {
			//ok, we are exposed on a port and need our own stuff installed...
			baseRouter.setInternalErrorRoute("org.webpieces.plugins.backend.BackendController.internalError");
			baseRouter.setPageNotFoundRoute("org.webpieces.plugins.backend.BackendController.notFound");
			baseRouter.addStaticDir(BOTH, "/assets/", "/html/", true);
			baseRouter.addStaticFile(BOTH, "/favicon.ico", "/favicon.ico", true);
		}
	}

	@Override
	protected RouteBuilder fetchBuilder(DomainRouteBuilder domainRouteBldr) {
		return domainRouteBldr.getBackendRouteBuilder();
	}
}
