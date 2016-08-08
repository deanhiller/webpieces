package org.webpieces.router.api.routing;

import java.io.File;
import java.util.Set;

import org.webpieces.router.api.HttpFilter;
import org.webpieces.router.api.dto.HttpMethod;

public abstract class AbstractRouteModule implements RouteModule {

	private Router router;

	@Override
	public final void configure(Router router, String currentPackage) {
		this.router = router;
		configure(currentPackage);
	}

	protected abstract void configure(String currentPackage);

	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		router.addRoute(method, path, controllerMethod, routeId);
	}

	public void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		router.addRoute(methods, path, controllerMethod, routeId);
	}

	public void addSecureRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		router.addSecureRoute(method, path, controllerMethod, routeId);
	}

	public void addSecureRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		router.addSecureRoute(methods, path, controllerMethod, routeId);
	}

	public void addStaticGetRoute(String path, File f) {
		router.addStaticGetRoute(path, f);
	}

	public void addFilter(String path, HttpFilter securityFilter) {
		router.addFilter(path, securityFilter);
	}

	public void setPageNotFoundRoute(String controllerMethod) {
		router.setPageNotFoundRoute(controllerMethod);
	}

	public void setInternalErrorRoute(String controllerMethod) {
		router.setInternalErrorRoute(controllerMethod);
	}

	public Router getScopedRouter(String path, boolean isSecure) {
		return router.getScopedRouter(path, isSecure);
	}

	
}
