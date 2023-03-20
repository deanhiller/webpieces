package org.webpieces.microsvc.api;

import org.webpieces.util.context.PlatformHeaders;

public enum MicroSvcHeader implements PlatformHeaders {

    REQUEST_ID("x-webpieces-requestid", true, "requestId", true),
    REQUEST_PATH(null, false, "requestPath", false),
    RECORDING("x-webpieces-recording", true, "recording", false),
    SECURE_TOKEN("x-webpieces-secure-token", false, null, false);

    private final String headerName;
    private final boolean isTransfer;
    private final String mdcKey;
    private final boolean isLoggable;

    MicroSvcHeader(String headerName, boolean isTransfer, String mdcKey, boolean isLoggable) {
        this.headerName = headerName;
        this.isTransfer = isTransfer;
        this.mdcKey = mdcKey;
        this.isLoggable = isLoggable;
    }

    @Override
    public String getHeaderName() {
        return headerName;
    }

    @Override
    public String getLoggerMDCKey() {
        return mdcKey;
    }

    @Override
    public boolean isWantLogged() {
        return isLoggable;
    }

    @Override
    public boolean isWantTransferred() {
        return isTransfer;
    }
}
