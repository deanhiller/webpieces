package org.webpieces.router.impl.loader.svc;

public class RouteInfoGeneric implements RouteData {
	private final boolean isCheckSecureToken;

	public RouteInfoGeneric(boolean isCheckSecureToken) {
		super();
		this.isCheckSecureToken = isCheckSecureToken;
	}
	
	public boolean isCheckSecureToken() {
		return isCheckSecureToken;
	}
}
