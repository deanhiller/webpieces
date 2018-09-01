package org.webpieces.webserver.api.login;

import org.webpieces.router.api.routing.RouteId;

public class LoginInfo {

	private String tokenThatExistsIfLoggedIn;
	private RouteId loginRouteId;
	private String securePath;
	private String[] secureFields;

	public LoginInfo(String securePath, String tokenThatExistsIfLoggedIn, RouteId loginRouteId, String ... secureFields) {
		this.securePath = securePath;
		this.tokenThatExistsIfLoggedIn = tokenThatExistsIfLoggedIn;
		this.loginRouteId = loginRouteId;
		this.secureFields = secureFields;
	}
	
	public LoginInfo(String tokenThatExistsIfLoggedIn, RouteId loginRouteId, String ... secureFields) {
		this.tokenThatExistsIfLoggedIn = tokenThatExistsIfLoggedIn;
		this.loginRouteId = loginRouteId;
		this.secureFields = secureFields;
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
	
	public String[] getSecureFields() {
		if(secureFields == null)
			return new String[0];
		return secureFields;
	}
}
