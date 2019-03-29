package org.webpieces.router.api.routing;

import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.ScopedRouteBuilder;

public abstract class ScopedRoutes implements Routes {
	
	@Override
	public final void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder routeBldr = domainRouteBldr.getAllDomainsRouteBuilder();
		String scope = getScope();
		ScopedRouteBuilder scopedRouter = routeBldr.getScopedRouteBuilder(scope);
		configure(routeBldr, scopedRouter);
	}
	
	/**
	 * Scoped routers have preference and within the scope of say "/backend" all routes are matching 
	 * what is after the "/backend" part of the url
	 * @return
	 */
	protected abstract String getScope();

	protected abstract void configure(RouteBuilder baseRouter, ScopedRouteBuilder scopedRouter);

}
