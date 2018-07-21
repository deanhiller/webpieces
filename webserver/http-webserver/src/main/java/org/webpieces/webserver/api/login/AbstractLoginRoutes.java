package org.webpieces.webserver.api.login;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;

public abstract class AbstractLoginRoutes extends AbstractRoutes {

	private String controller;
	private String securePath;
	private String basePath;

	public AbstractLoginRoutes(String controller, String basePath, String securePath) {
		this.controller = controller;
		this.basePath = basePath;
		this.securePath = securePath;
	}
	
	@Override
	protected void configure() {
		Router httpsRouter = router.getScopedRouter(basePath, true);
		httpsRouter.addRoute(GET , "/logout",              controller+".logout", getRenderLogoutRoute());		
		httpsRouter.addRoute(GET , "/login",               controller+".login", getRenderLoginRoute());
		httpsRouter.addRoute(POST, "/postLogin",           controller+".postLogin", getPostLoginRoute());

		addLoggedInHome(httpsRouter);
		
		addFilter(securePath, LoginFilter.class, new LoginInfo(getSessionToken(), getRenderLoginRoute()), PortType.HTTPS_FILTER);
		//redirects all queries for non-existent pages to a login (then the clients don't know which urls exist and don't exist which is good)
		//ie. you can only get not found AFTER logging in
		addNotFoundFilter(LoginFilter.class, new LoginInfo(securePath, getSessionToken(), getRenderLoginRoute()), PortType.HTTPS_FILTER);
	}

	protected abstract void addLoggedInHome(Router router);

	protected abstract String getSessionToken();

	protected abstract RouteId getPostLoginRoute();

	protected abstract RouteId getRenderLoginRoute();

	protected abstract RouteId getRenderLogoutRoute();

}
