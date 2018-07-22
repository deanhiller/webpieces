package org.webpieces.plugins.backend;

import org.webpieces.router.api.routing.ScopedRoutes;

public class BackendRoutes extends ScopedRoutes {
	
	public static final String BACKEND_ROUTE = "/@backend";
	
    @Override
    protected String getScope() {
        return BACKEND_ROUTE;
    }

    @Override
    protected final boolean isHttpsOnlyRoutes() {
        return true;
    }

    @Override
    protected void configure() {

    }
}
