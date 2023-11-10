package org.webpieces.microsvc.api;

import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;

public enum MicroSvcHeader implements PlatformHeaders {

    REQUEST_ID("x-webpieces-requestid", "requestId", true, false),
    PREVIOUS_REQUEST_ID("x-webpieces-previousid", "previousId", true, false),

    REQUEST_PATH(null, "requestPath", false, false),
    RECORDING("x-webpieces-recording","recording", false, false),

    SECURE_TOKEN("x-webpieces-secure-token", null, false, true),
    FILTER_CHAIN("x-webpieces-svcchain",null, false, false);

    private final String headerName;
    private final String mdcKey;
    private final boolean isLoggable;
    private final boolean isSecured;
    private final boolean isDimensionForMetrics;

    MicroSvcHeader(String headerName, String mdcKey, boolean isLoggable, boolean isSecured, boolean isDimensionForMetrics) {
        this.headerName = headerName;
        this.mdcKey = mdcKey;
        this.isLoggable = isLoggable;
        this.isSecured = isSecured;
        this.isDimensionForMetrics = isDimensionForMetrics;
    }

    MicroSvcHeader(String headerName, String mdcKey, boolean isLoggable, boolean isSecured) {
        this(headerName, mdcKey, isLoggable, isSecured, false);
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
        return this.headerName != null;
    }

    @Override
    public boolean isSecured() {
        return this.isSecured;
    }

    @Override
    public boolean isDimensionForMetrics() { return this.isDimensionForMetrics; }


    public static Restore newRequestId(String newRequestId) {
        String reqId = Context.getMagic(REQUEST_ID);
        String prevReq = Context.getMagic(PREVIOUS_REQUEST_ID);

        Context.putMagic(REQUEST_ID, newRequestId);
        Context.putMagic(PREVIOUS_REQUEST_ID, prevReq);

        return new Restore(reqId, prevReq);
    }

}
