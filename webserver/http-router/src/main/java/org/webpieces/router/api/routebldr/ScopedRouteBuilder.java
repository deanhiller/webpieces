package org.webpieces.router.api.routebldr;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;

public interface ScopedRouteBuilder {

	void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId);
	
	/**
	 * Use this route to add POST routes with no token check(for apis generally or to turn of the security IF you really
	 * want to).  Also, use this route to add form that use the GET method so the token is checked as well.
	 */
	void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken);

	/**
	 * A pure content route has no RouteId.  This is generally used for apis since RouteId are used for reverse
	 * translation from web templates like @[RouteId.MyRouteId]@ would be reverse translated to some url
	 * While this sets up a controller that is invoked and will just return content with no need to reverse
	 * translate urls
	 */
	void addContentRoute(Port port, HttpMethod method, String urlPath, String controllerMethod);
	
	void addCrud(Port port, String entity, String controller, CrudRouteIds routeIds);

	/**
	 * If on the classpath, we use classloader and InputStream.  If not, we use memory mapped files in
	 * hopes that it performs better AND asyncrhonously read such that thread goes and does other 
	 * work until the completionListener callback using AsynchronousFileChannel
	 */
	void addStaticDir(Port port, String urlPath, String fileSystemPath, boolean isOnClassPath);

	/**
	 * If on the classpath, we use classloader and InputStream.  If not, we use memory mapped files in
	 * hopes that it performs better AND asyncrhonously read such that thread goes and does other 
	 * work until the completionListener callback using AsynchronousFileChannel
	 */
	void addStaticFile(Port port, String urlPath, String fileSystemPath, boolean isOnClassPath);
	
	/**
	 * Any routes added to this, first match the prefix of the url to path 
	 * 
	 * @param path
	 * @return
	 */
	ScopedRouteBuilder getScopedRouteBuilder(String path);
}
