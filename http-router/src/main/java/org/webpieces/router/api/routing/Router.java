package org.webpieces.router.api.routing;

import java.io.File;
import java.util.Set;

import org.webpieces.router.api.HttpFilter;
import org.webpieces.router.api.dto.HttpMethod;

/**
 * You can override this implementation if you like
 * 
 * @author dhiller
 *
 */
public interface Router {

	void addPostRoute(String path, String controllerMethod);
	
	void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId);
	
	void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId);

	//later maybe when we move Route to the api
	//void addRoute(Route r, RouteId routeId);
	
	void addSecurePostRoute(String path, String controllerMethod);
	
	void addSecureRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId);
	void addSecureRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId);

	//if f.isDirectory, verify path endsWith("/") then
	void addStaticGetRoute(String path, File f);
	
	void addFilter(String path, HttpFilter securityFilter);

	/**
	 * This is the controller for 404's where the path was not found AND this MUST be set
	 */
	void setCatchAllRoute(String controllerMethod);
	
	//later maybe when we move Route to the api
	//void setCatchAllRoute(Route r);
	
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
