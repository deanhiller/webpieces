package org.webpieces.plugins.backend;

import org.webpieces.router.api.routes.ScopedRoutes;

public abstract class BackendRoutes extends ScopedRoutes {
	
	public static final String BACKEND_ROUTE = "/@backend";
	
    @Override
    protected String getScope() {
        return BACKEND_ROUTE;
    }

}
