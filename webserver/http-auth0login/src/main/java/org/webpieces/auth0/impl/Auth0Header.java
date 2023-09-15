package org.webpieces.auth0.impl;

import org.webpieces.util.context.PlatformHeaders;

public enum Auth0Header implements PlatformHeaders {

    AUTH_TOKEN("Authorization", null, false, true);

    private final String headerName;
    private final String logKey;
    private final boolean isLog;
    private final boolean isSecure;

    Auth0Header(String headerName, String logKey, boolean isLog, boolean isSecure) {
        this.headerName = headerName;
        this.logKey = logKey;
        this.isLog = isLog;
        this.isSecure = isSecure;
    }


    @Override
    public String getHeaderName() {
        return headerName;
    }

    @Override
    public String getLoggerMDCKey() {
        return logKey;
    }

    @Override
    public boolean isWantLogged() {
        return isLog;
    }

    @Override
    public boolean isWantTransferred() {
        return headerName != null;
    }

    @Override
    public boolean isSecured() {
        return isSecure;
    }
}
