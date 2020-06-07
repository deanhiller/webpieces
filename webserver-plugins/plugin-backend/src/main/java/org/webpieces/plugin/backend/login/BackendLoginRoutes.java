package org.webpieces.plugin.backend.login;

import static org.webpieces.router.api.routes.Port.BOTH;

import java.util.function.Supplier;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.webserver.api.login.AbstractLoginRoutes;

public class BackendLoginRoutes extends AbstractLoginRoutes {

	private Supplier<Boolean> isUsePluginAssets;

	/**
	 * @param controller
	 * @param basePath The 'unsecure' path that has a login page so you can get to the secure path
	 * @param securePath The path for the secure filter that ensures everyone under that path is secure
	 */
	public BackendLoginRoutes(Supplier<Boolean> isUsePluginAssets, String controller, String basePath, String securePackageRegEx) {
		//this filter is best up very high in the stack such that no other filters run if person is not logged in
		super(controller, basePath, securePackageRegEx, true, 9999, "password");
		this.isUsePluginAssets = isUsePluginAssets;
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
		scopedRouter.addRoute(Port.HTTPS, HttpMethod.GET ,   "/loggedinhome",        "org.webpieces.plugin.backend.BackendController.home", BackendLoginRouteId.BACKEND_LOGGED_IN_HOME);
		
		ScopedRouteBuilder scoped2 = baseRouter.getScopedRouteBuilder(basePath);
		scoped2.addRoute(Port.BOTH, HttpMethod.GET,  "", "org.webpieces.plugin.backend.BackendController.redirectToLogin", BackendLoginRouteId.LOGGED_OUT_LANDING);
		
		if(isUsePluginAssets.get()) {
			//ok, we are exposed on a port and need our own stuff installed...
			baseRouter.addRoute(Port.BOTH, HttpMethod.GET, "/", "org.webpieces.plugin.backend.BackendController.redirectHome", BackendLoginRouteId.REDIRECT_TO_HOME);
			baseRouter.setInternalErrorRoute("org.webpieces.plugin.backend.BackendController.internalError");
			baseRouter.setPageNotFoundRoute("org.webpieces.plugin.backend.BackendController.notFound");
			baseRouter.addStaticDir(BOTH, "/assets/", "/html/", true);
			baseRouter.addStaticFile(BOTH, "/favicon.ico", "/favicon.ico", true);
		}
	}

	@Override
	protected RouteBuilder fetchBuilder(DomainRouteBuilder domainRouteBldr) {
		return domainRouteBldr.getBackendBuilder().getBldrForAllOtherContentTypes();
	}
}
