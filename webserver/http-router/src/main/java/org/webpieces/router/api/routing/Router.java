package org.webpieces.router.api.routing;

import java.io.File;
import java.util.Set;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.HttpFilter;

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

	//if f.isDirectory, verify path endsWith("/") then
	void addStaticGetRoute(String path, File f);
	
	void addFilter(String path, HttpFilter securityFilter);

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
	Router getScopedRouter(String path, boolean isSecure);

}
