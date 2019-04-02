package org.webpieces.router.impl.loader.svc;

public class RouteInfoGeneric implements RouteInfo {
	private final boolean isCheckSecureToken;

	public RouteInfoGeneric(boolean isCheckSecureToken) {
		super();
		this.isCheckSecureToken = isCheckSecureToken;
	}
	
	public boolean isCheckSecureToken() {
		return isCheckSecureToken;
	}
}
