package org.webpieces.router.api.routebldr;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;

import java.util.List;

public interface ScopedRouteBuilder {

	void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId);
	
	/**
	 * Use this route to add POST routes with no token check(for apis generally or to turn of the security IF you really
	 * want to).  Also, use this route to add form that use the GET method so the token is checked as well.
	 */
	void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken);

	/**
	 * A pure content route has no RouteId UNLESS you want to call it from javascript as you will need the route id
	 * to reverse the route id to url so you can change the url in one place.  This method thoough is generally used 
	 * for apis since RouteId is not passed in.
	 */
	void addContentRoute(Port port, HttpMethod method, String urlPath, String controllerMethod);

	/**
	 * Only use this method if you plan on calling this endpoint from your website and if you do, you can use the
	 * html webpieces tags like @[RouteId.MyRouteId]@ in the javascript so that the url is generated for you.
	 * In this way, the url only exists in one place in your entire app. 
	 */
	void addContentRoute(Port port, HttpMethod method, String urlPath, String controllerMethod, RouteId routeId);

	/**
	 * Re-usable components like RESTApiRoutes.java can force using a certain binder which leads to
	 * 100% pure java and no dependencies on the platform.
	 */
	void addContentRoute(Port port, HttpMethod method, String urlPath, String controllerMethod,
						 Class<? extends BodyContentBinder> binder);

	/**
	 * Adds a streaming route where the method in the controller should be of the form
	 * 
	 * public XFuture<StreamWriter> processRequest(RouterRequest request, RouteStreamHandle stream);
	 * 
	 * You can do bi-directional communication in http1.1 or http2 with this route.  Just keep writing to the
	 * RouteStreamHandle and the client will keep writing to the StreamWriter that you return.
	 */
	void addStreamRoute(Port both, HttpMethod get, String path, String controllerMethod);
	
	void addStreamRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId);

	void addCrud(Port port, String entity, String controller, CrudRouteIds routeIds);
	
	/**
	 * Any routes added to this, first match the prefix of the url to path 
	 * 
	 * @param path
	 * @return
	 */
	ScopedRouteBuilder getScopedRouteBuilder(String path);

}
