package org.webpieces.router.api.routing;

public class BackendRoutes extends ScopedRoutes {
    @Override
    protected String getScope() {
        return "/backend";
    }

    @Override
    protected final boolean isHttpsOnlyRoutes() {
        return true;
    }

    @Override
    protected void configure() {

    }
}
