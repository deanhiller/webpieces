package org.webpieces.router.api;

import java.util.Set;

public interface Router {

	void addRoute(RouteId routeId, HttpMethod method, String path, Object controller, String controllerMethod);

	void addRoute(RouteId routeId, Set<HttpMethod> methods, String path, Object controller, String controllerMethod);

	void addFilter(String path, HttpFilter securityFilter);

}
