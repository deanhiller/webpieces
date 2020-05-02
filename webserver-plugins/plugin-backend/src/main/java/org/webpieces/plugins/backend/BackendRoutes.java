package org.webpieces.plugins.backend;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.Routes;

public abstract class BackendRoutes implements Routes {
	
	public static final String BACKEND_ROUTE = "/@backend";
	
    protected String getScope() {
        return BACKEND_ROUTE;
    }

	@Override
	public final void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder routeBldr = domainRouteBldr.getBackendBuilder().getBldrForAllOtherContentTypes();
		String scope = getScope();
		ScopedRouteBuilder scopedRouter = routeBldr.getScopedRouteBuilder(scope);
		configure(routeBldr, scopedRouter);
	}
	
	protected abstract void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr);
}
