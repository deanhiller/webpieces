package org.webpieces.router.impl.services;

import org.webpieces.ctx.api.HttpMethod;

public class RouteInfoForHtml implements RouteData {
	private final boolean isCheckSecureToken;
	private final HttpMethod httpMethod;

	public RouteInfoForHtml(boolean isCheckSecureToken, HttpMethod httpMethod) {
		super();
		this.isCheckSecureToken = isCheckSecureToken;
		this.httpMethod = httpMethod;
	}
	
	public boolean isCheckSecureToken() {
		return isCheckSecureToken;
	}

	public boolean isPostOnly() {
		return httpMethod == HttpMethod.POST;
	}
}
