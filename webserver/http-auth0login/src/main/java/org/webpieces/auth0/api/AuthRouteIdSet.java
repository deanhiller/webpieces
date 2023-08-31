package org.webpieces.auth0.api;

import org.webpieces.router.api.routes.RouteId;

public class AuthRouteIdSet {
    private RouteId callbackRoute;
    private RouteId loginRoute;
    private RouteId logoutRoute;

    private RouteId toRenderAfterLogin;
    private RouteId toRenderAfterLogout;

    private RouteId loginDeclinedRoute;

    private String[] secureFields;

    public AuthRouteIdSet(
            RouteId callbackRoute,
            RouteId loginRoute,
            RouteId logoutRoute,
            RouteId toRenderAfterLogin,
            RouteId toRenderAfterLogout,
            RouteId loginDeclinedRoute,
            String ... secureFields
    ) {
        this.callbackRoute = callbackRoute;
        this.loginRoute = loginRoute;
        this.logoutRoute = logoutRoute;
        this.toRenderAfterLogin = toRenderAfterLogin;
        this.toRenderAfterLogout = toRenderAfterLogout;
        this.loginDeclinedRoute = loginDeclinedRoute;
        this.secureFields = secureFields;
    }

    public RouteId getCallbackRoute() {
        return callbackRoute;
    }

    public RouteId getLoginRoute() {
        return loginRoute;
    }

    public RouteId getLogoutRoute() {
        return logoutRoute;
    }

    public RouteId getToRenderAfterLogin() {
        return toRenderAfterLogin;
    }

    public RouteId getToRenderAfterLogout() {
        return toRenderAfterLogout;
    }

    public RouteId getLoginDeclinedRoute() {
        return loginDeclinedRoute;
    }

    public String[] getSecureFields() {
        return secureFields;
    }
}
