package org.webpieces.router.api.routing;

public interface MultiRoute {

	void addHtmlRoute(String controllerMethod, RouteId routeId);

	void addHtmlRoute(String controllerMethod, RouteId routeId, boolean checkToken);

	void addContentRoute(String controllerMethod, String contentType);
	
}
