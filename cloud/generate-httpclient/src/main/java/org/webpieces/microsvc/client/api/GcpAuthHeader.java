package org.webpieces.microsvc.client.api;

import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.PlatformHeaders;

/**
 *
 */
public enum GcpAuthHeader implements PlatformHeaders {

    AUTH_TOKEN("Authorization", null, false, true),
    METADATA_FLAVOR("Metadata-Flavor", null, false, false);

    private final String headerName;
    private final String logKey;
    private final boolean isLog;
    private final boolean isSecure;

    GcpAuthHeader(String headerName, String logKey, boolean isLog, boolean isSecure) {
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

    @Override
    public boolean isDimensionForMetrics() {
        return false;
    }

}
