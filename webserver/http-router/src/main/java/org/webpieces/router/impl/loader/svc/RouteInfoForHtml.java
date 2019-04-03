package org.webpieces.router.impl.loader.svc;

public class RouteInfoForHtml implements RouteData {
	private final boolean isCheckSecureToken;

	public RouteInfoForHtml(boolean isCheckSecureToken) {
		super();
		this.isCheckSecureToken = isCheckSecureToken;
	}
	
	public boolean isCheckSecureToken() {
		return isCheckSecureToken;
	}
}
