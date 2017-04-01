package org.webpieces.webserver.api.login;

import org.webpieces.router.api.routing.RouteId;

public class LoginInfo {

	public static final String LOGIN_TOKEN1 = "userId";

	private String tokenThatExistsIfLoggedIn;
	private RouteId loginRouteId;

	public LoginInfo(String tokenThatExistsIfLoggedIn, RouteId loginRouteId) {
		this.tokenThatExistsIfLoggedIn = tokenThatExistsIfLoggedIn;
		this.loginRouteId = loginRouteId;
	}

	public String getTokenThatExistsIfLoggedIn() {
		return tokenThatExistsIfLoggedIn;
	}

	public RouteId getLoginRouteId() {
		return loginRouteId;
	}
	
}
