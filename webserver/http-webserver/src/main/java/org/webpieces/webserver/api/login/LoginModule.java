package org.webpieces.webserver.api.login;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.PortType;

public class LoginModule extends AbstractRouteModule {

	private String controller;
	private String securePath;
	private String sessionToken;

	public LoginModule(String controller, String securePath) {
		this(controller, securePath, LoginInfo.LOGIN_TOKEN1);
	}
	
	public LoginModule(String controller, String securePath, String sessionToken) {
		this.controller = controller;
		this.securePath = securePath;
		this.sessionToken = sessionToken;
	}
	
	@Override
	protected void configure() {
		
		addHttpsRoute(GET , "/logout",              controller+".logout", LoginRouteId.LOGOUT);		
		addHttpsRoute(GET , "/login",               controller+".login", LoginRouteId.LOGIN);
		addHttpsRoute(POST, "/postLogin",           controller+".postLogin", LoginRouteId.POST_LOGIN);
		
		addFilter(securePath, LoginFilter.class, new LoginInfo(sessionToken, LoginRouteId.LOGIN), PortType.HTTPS_FILTER);
		
	}

}
