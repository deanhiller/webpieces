package org.webpieces.router.api.routing;

import java.util.List;
import java.util.Set;

import org.webpieces.ctx.api.HttpMethod;

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

	public void addMultiRoute(HttpMethod method, List<String> paths, String controllerMethod, RouteId addRoute) {
		router.addMultiRoute(method, paths, controllerMethod, addRoute);
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

	public void addHttpsMultiRoute(HttpMethod method, List<String> paths, String controllerMethod, RouteId addRoute) {
		router.addHttpsMultiRoute(method, paths, controllerMethod, addRoute);
	}
	
	public void addStaticDir(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		router.addStaticDir(urlPath, fileSystemPath, isOnClassPath);
	}
	
	public void addStaticFile(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		router.addStaticFile(urlPath, fileSystemPath, isOnClassPath);
	}
	
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		router.addFilter(path, filter, initialConfig, type);
	}

	public <T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		router.addNotFoundFilter(filter, initialConfig, type);
	}

	public <T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		router.addNotFoundFilter(filter, initialConfig, type);
	}
	
	public void setPageNotFoundRoute(String controllerMethod) {
		router.setPageNotFoundRoute(controllerMethod);
	}

	public void setInternalErrorRoute(String controllerMethod) {
		router.setInternalErrorRoute(controllerMethod);
	}

	public Router getScopedRouter(String path) {
		return router.getScopedRouter(path);
	}	
	
}
