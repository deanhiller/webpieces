package org.webpieces.googleauth.api;

import org.webpieces.router.api.routes.RouteId;

public class GoogleAuthConfig {
    private final RouteId toRenderAfterLogin;
    private final RouteId toRenderAfterLogout;
    private final RouteId loginDeclinedRoute;
    private final String packageRegEx;
    private final int filterLevel;

    //scopes
    //send - https://www.googleapis.com/auth/gmail.send
    //update labels - https://www.googleapis.com/auth/gmail.labels
    //read (and scope for watch api) - https://www.googleapis.com/auth/gmail.readonly
    //watch api ->
    // https://developers.google.com/gmail/api/reference/rest/v1/users/watch
    // https://developers.google.com/gmail/api/guides/push

    private final String gcpScopes;

    private final String[] secureFields;

    public GoogleAuthConfig(
            RouteId toRenderAfterLogin,
            RouteId toRenderAfterLogout,
            RouteId loginDeclinedRoute,
            String packageRegEx,
            int filterLevel,
            String gcpScopes,
            String ... secureFields
    ) {
        this.toRenderAfterLogin = toRenderAfterLogin;
        this.toRenderAfterLogout = toRenderAfterLogout;
        this.loginDeclinedRoute = loginDeclinedRoute;
        this.packageRegEx = packageRegEx;
        this.filterLevel = filterLevel;
        this.gcpScopes = gcpScopes;
        this.secureFields = secureFields;
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

    public String getPackageRegEx() {
        return packageRegEx;
    }

    public int getFilterLevel() {
        return filterLevel;
    }

    public String getGcpScopes() {
        return gcpScopes;
    }

}
