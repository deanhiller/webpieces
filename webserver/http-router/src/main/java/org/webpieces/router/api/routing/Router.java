package org.webpieces.router.api.routing;

import java.util.Set;

import org.webpieces.ctx.api.HttpMethod;

/**
 * You can override this implementation if you like
 * 
 * @author dhiller
 *
 */
public interface Router {

	void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId);
	/**
	 * Use this route to add POST routes with no token check(for apis generally or to turn of the security IF you really
	 * want to).  Also, use this route to add form that use the GET method so the token is checked as well.
	 */
	void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken);

	void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId);

	void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId);
	
	/**
	 * Allows you to add POST route with checkToken=false(other addRoutes are defaulted to true) AND allows a GET route
	 * that does a secure token check(IF you post with GET method which is highly NOT recommended)
	 */
	void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken);
	void addHttpsRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId);

	/**
	 * If on the classpath, we use classloader and InputStream.  If not, we use memory mapped files in
	 * hopes that it performs better AND asyncrhonously read such that thread goes and does other 
	 * work until the completionListener callback using AsynchronousFileChannel
	 */
	void addStaticDir(String urlPath, String fileSystemPath, boolean isOnClassPath);

	/**
	 * If on the classpath, we use classloader and InputStream.  If not, we use memory mapped files in
	 * hopes that it performs better AND asyncrhonously read such that thread goes and does other 
	 * work until the completionListener callback using AsynchronousFileChannel
	 */
	void addStaticFile(String urlPath, String fileSystemPath, boolean isOnClassPath);

	<T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type);

	<T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type);

	<T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type);

	/**
	 * This is the controller for 404's where the path was not found AND this MUST be set
	 */
	void setPageNotFoundRoute(String controllerMethod);
	
	void setInternalErrorRoute(String controllerMethod);
	
	/**
	 * If you scope your router to /backend, every Router.addRoute path uses that prefix as the final
	 * path
	 * 
	 * @param path
	 * @param isSecure true if only available over https otherwise available over http and https
	 * @return
	 */
	Router getScopedRouter(String path);
	
	void addCrud(String entity, String controller, 
			RouteId listRoute, RouteId addRoute, RouteId editRoute,
			RouteId saveRoute, RouteId confirmDelete, RouteId deleteRoute);

}
