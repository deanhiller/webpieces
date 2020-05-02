package org.webpieces.router.api.routebldr;

import org.webpieces.router.api.routes.RouteFilter;

/**
 * This is a builder for when you want to route based on request Content-Type header in that if a request matches
 * the Content-Type header, we route the request into all these routes.  If there is no matching route, then
 * NotFound is returned.  You need to add the not found filters and internal error filters yourself 
 * 
 * @author dean
 *
 */
public interface ContentTypeRouteBuilder {

	/**
	 * Port is forced to Port.HTTPS
	 * HttpMethod is forced to HttpMethod.POST
	 */
	void addRoute(String path, String controllerMethod);

	/**
	 * 
	 * @param <T>
	 * @param path
	 * @param filter
	 * @param initialConfig
	 * @param filterApplyLevel Higher means higher in the stack of filters.  Plugins MUST require a configuration level for any filters they install
	 */
	<T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, int filterApplyLevel);

	
}
