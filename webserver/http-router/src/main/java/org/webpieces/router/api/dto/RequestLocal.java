package org.webpieces.router.api.dto;

public class RequestLocal {

	private static ThreadLocal<RouterRequest> routerRequest = new ThreadLocal<>();
	
	public static RouterRequest getRequest() {
		return routerRequest.get();
	}
	
	public static void setRequest(RouterRequest req) {
		routerRequest.set(req);
	}
}
