package org.webpieces.webserver.api.login;

import org.webpieces.router.api.routing.RouteId;

public class LoginInfo {

	private String tokenThatExistsIfLoggedIn;
	private RouteId loginRouteId;
	private String securePath;

	public LoginInfo(String securePath, String tokenThatExistsIfLoggedIn, RouteId loginRouteId) {
		this.securePath = securePath;
		this.tokenThatExistsIfLoggedIn = tokenThatExistsIfLoggedIn;
		this.loginRouteId = loginRouteId;
	}
	
	public LoginInfo(String tokenThatExistsIfLoggedIn, RouteId loginRouteId) {
		this.tokenThatExistsIfLoggedIn = tokenThatExistsIfLoggedIn;
		this.loginRouteId = loginRouteId;
	}
	
	public String getSecurePath() {
		return securePath;
	}


	public String getTokenThatExistsIfLoggedIn() {
		return tokenThatExistsIfLoggedIn;
	}

	public RouteId getLoginRouteId() {
		return loginRouteId;
	}
	
}
