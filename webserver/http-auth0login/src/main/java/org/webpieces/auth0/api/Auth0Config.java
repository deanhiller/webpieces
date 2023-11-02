package org.webpieces.auth0.api;

import org.webpieces.router.api.routes.RouteId;

public class Auth0Config {
    private RouteId toRenderAfterLogin;
    private RouteId toRenderAfterLogout;
    private RouteId loginDeclinedRoute;
    private String packageRegEx;
    private int filterLevel;

    //scopes
    //send - https://www.googleapis.com/auth/gmail.send
    //update labels - https://www.googleapis.com/auth/gmail.labels
    //read (and scope for watch api) - https://www.googleapis.com/auth/gmail.readonly
    //watch api ->
    // https://developers.google.com/gmail/api/reference/rest/v1/users/watch
    // https://developers.google.com/gmail/api/guides/push

    private String gmailScopes = "openid profile email phone offline_access";

    private String[] secureFields;

    public Auth0Config(
            RouteId toRenderAfterLogin,
            RouteId toRenderAfterLogout,
            RouteId loginDeclinedRoute,
            String packageRegEx,
            int filterLevel,
            String gmailScopes,
            String ... secureFields
    ) {
        this.toRenderAfterLogin = toRenderAfterLogin;
        this.toRenderAfterLogout = toRenderAfterLogout;
        this.loginDeclinedRoute = loginDeclinedRoute;
        this.packageRegEx = packageRegEx;
        this.filterLevel = filterLevel;
        this.gmailScopes = gmailScopes;
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

    public String getGmailScopes() {
        return gmailScopes;
    }

}
