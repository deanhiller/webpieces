package org.webpieces.auth0.api;

import org.webpieces.router.api.routes.RouteId;

public class Auth0Config {
    private RouteId callbackRoute;
    private RouteId loginRoute;
    private RouteId logoutRoute;

    private RouteId toRenderAfterLogin;
    private RouteId toRenderAfterLogout;

    private RouteId loginDeclinedRoute;

    private String controller;
    private String packageRegEx;
    private int filterLevel;

    private String[] secureFields;

    public Auth0Config(
            RouteId callbackRoute,
            RouteId loginRoute,
            RouteId logoutRoute,
            RouteId toRenderAfterLogin,
            RouteId toRenderAfterLogout,
            RouteId loginDeclinedRoute,
            String controller,
            String packageRegEx,
            int filterLevel,
            String ... secureFields
    ) {
        this.callbackRoute = callbackRoute;
        this.loginRoute = loginRoute;
        this.logoutRoute = logoutRoute;
        this.toRenderAfterLogin = toRenderAfterLogin;
        this.toRenderAfterLogout = toRenderAfterLogout;
        this.loginDeclinedRoute = loginDeclinedRoute;
        this.controller = controller;
        this.packageRegEx = packageRegEx;
        this.filterLevel = filterLevel;
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

    public String getController() {
        return controller;
    }

    public String getPackageRegEx() {
        return packageRegEx;
    }

    public int getFilterLevel() {
        return filterLevel;
    }
}
