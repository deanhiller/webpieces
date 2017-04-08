package org.webpieces.webserver.api.login;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRoutes;
import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.Router;

public class LoginRoutes extends AbstractRoutes {

	private String controller;
	private String securePath;
	private String sessionToken;

	public LoginRoutes(String controller, String securePath) {
		this(controller, securePath, LoginInfo.LOGIN_TOKEN1);
	}
	
	public LoginRoutes(String controller, String securePath, String sessionToken) {
		this.controller = controller;
		this.securePath = securePath;
		this.sessionToken = sessionToken;
	}
	
	@Override
	protected void configure() {
		
		Router httpsRouter = router.getScopedRouter(null, true);
		httpsRouter.addRoute(GET , "/logout",              controller+".logout", LoginRouteId.LOGOUT);		
		httpsRouter.addRoute(GET , "/login",               controller+".login", LoginRouteId.LOGIN);
		httpsRouter.addRoute(POST, "/postLogin",           controller+".postLogin", LoginRouteId.POST_LOGIN);
		
		addFilter(securePath, LoginFilter.class, new LoginInfo(sessionToken, LoginRouteId.LOGIN), PortType.HTTPS_FILTER);
		addNotFoundFilter(LoginFilter.class, new LoginInfo(sessionToken, LoginRouteId.LOGIN), PortType.HTTPS_FILTER);
	}

}
