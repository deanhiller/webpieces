package org.webpieces.router.api.routing;

import java.io.File;
import java.util.Set;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.HttpFilter;

public abstract class AbstractRouteModule implements RouteModule {

	private Router router;

	@Override
	public final void configure(Router router, String currentPackage) {
		this.router = router;
		configure(currentPackage);
	}

	protected abstract void configure(String currentPackage);

	/**
	 * This is the bundle name as in something like org.webpieces.messages where
	 * that will use org/webpieces/messages.properties on the classpath for the default
	 * local or another one for another language
	 */
	@Override
	public String getI18nBundleName() {
		Class<? extends AbstractRouteModule> clazz = getClass();
		String name = clazz.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if(lastIndexOf < 0)
			return "messages";
		
		String packageName = name.substring(0, lastIndexOf);
		return packageName+".messages";
	}
	
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		router.addRoute(method, path, controllerMethod, routeId);
	}

	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken) {
		router.addRoute(method, path, controllerMethod, routeId, checkToken);
	}

	public void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		router.addRoute(methods, path, controllerMethod, routeId);
	}

	public void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		router.addHttpsRoute(method, path, controllerMethod, routeId);
	}

	public void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken) {
		router.addHttpsRoute(method, path, controllerMethod, routeId, checkToken);
	}
	
	public void addHttpsRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		router.addHttpsRoute(methods, path, controllerMethod, routeId);
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
